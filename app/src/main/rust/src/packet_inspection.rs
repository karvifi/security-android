use std::net::{Ipv4Addr};
use ahash::AHashSet;

pub struct PacketInspector {
    threat_ips: AHashSet<u32>,
}

impl PacketInspector {
    pub fn new() -> Self {
        Self {
            threat_ips: AHashSet::new(),
        }
    }

    pub fn analyze(&self, packet: &[u8]) -> u8 {
        if packet.len() < 20 {
            return 0; // Invalid packet
        }

        let ip_version = (packet[0] >> 4) & 0x0F;

        match ip_version {
            4 => self.analyze_ipv4(packet),
            6 => self.analyze_ipv6(packet),
            _ => 0,
        }
    }

    fn analyze_ipv4(&self, packet: &[u8]) -> u8 {
        if packet.len() < 20 { return 0; }

        let protocol = packet[9];
        let dst_ip = u32::from_be_bytes([packet[16], packet[17], packet[18], packet[19]]);

        // Quick IP check
        if self.threat_ips.contains(&dst_ip) {
            return 1; // MALICIOUS_IP
        }

        match protocol {
            6 => self.analyze_tcp(packet),   // TCP
            17 => self.analyze_udp(packet),  // UDP
            1 => self.analyze_icmp(packet),  // ICMP
            _ => 0,
        }
    }

    fn analyze_ipv6(&self, _packet: &[u8]) -> u8 {
        // Simplified IPv6 handling for now
        0
    }

    fn analyze_tcp(&self, packet: &[u8]) -> u8 {
        if packet.len() < 40 {
            return 0;
        }

        // TCP header offset assumes no IP options; offset 20..
        let src_port = u16::from_be_bytes([packet[20], packet[21]]);
        let dst_port = u16::from_be_bytes([packet[22], packet[23]]);

        // Check for common data-exfiltration and malware ports
        if Self::is_known_c2_port(dst_port) {
            return 1; // MALICIOUS (C2)
        }

        // HTTP
        if src_port == 80 || dst_port == 80 {
            match self.inspect_http(packet) {
                Some(analysis) => return analysis,
                None => {}
            }
        }

        // TLS/HTTPS
        if src_port == 443 || dst_port == 443 {
            match self.inspect_tls(packet) {
                Some(analysis) => return analysis,
                None => {}
            }
        }

        0 // Allow by default
    }

    fn analyze_udp(&self, packet: &[u8]) -> u8 {
        if packet.len() < 28 { return 0; }

        let dst_port = u16::from_be_bytes([packet[22], packet[23]]);

        // DNS (53) or mDNS (5353)
        if dst_port == 53 || dst_port == 5353 {
            return self.inspect_dns(packet);
        }

        // Large UDP packets may indicate tunneling
        if packet.len() > 512 {
            return 3; // SUSPICIOUS_LARGE_UDP
        }

        0
    }

    fn analyze_icmp(&self, _packet: &[u8]) -> u8 {
        // ICMP generally not used for exfiltration; monitor for odd sizes
        0
    }

    /// Inspect HTTP payload for sensitive strings or large uploads
    /// returns Some(code) if action required, None for no decision
    fn inspect_http(&self, packet: &[u8]) -> Option<u8> {
        // Calculate TCP header offset (IP header length may vary)
        let ihl = (packet[0] & 0x0F) as usize * 4;
        if packet.len() <= ihl + 20 { return None; }

        // TCP header length (data offset nibble)
        let tcp_offset = ihl;
        let data_offset = ((packet[tcp_offset + 12] >> 4) as usize) * 4;
        let payload_offset = ihl + data_offset;
        if packet.len() <= payload_offset { return None; }

        let payload = &packet[payload_offset..];
        // Inspect a bounded prefix to avoid heavy parsing
        let sample = &payload[..payload.len().min(2048)];

        if Self::bytes_contains_case_insensitive(sample, b"password") ||
           Self::bytes_contains_case_insensitive(sample, b"api_key") ||
           Self::bytes_contains_case_insensitive(sample, b"token") ||
           Self::bytes_contains_case_insensitive(sample, b"authorization") {
            return Some(2); // SENSITIVE_DATA
        }

        // Large POSTs
        if sample.len() > 1024 * 1024 {
            return Some(1); // MALICIOUS (large upload)
        }

        None
    }

    /// Inspect TLS client hello fingerprint for known malicious JA3-like patterns
    fn inspect_tls(&self, packet: &[u8]) -> Option<u8> {
        // Very simplified: look for "Client Hello" marker and a small prefix
        let ihl = (packet[0] & 0x0F) as usize * 4;
        if packet.len() <= ihl + 20 { return None; }

        let tcp_offset = ihl;
        let data_offset = ((packet[tcp_offset + 12] >> 4) as usize) * 4;
        let payload_offset = ihl + data_offset;
        if packet.len() <= payload_offset + 5 { return None; }

        let payload = &packet[payload_offset..];

        // TLS records start with 0x16 for handshake, then version
        if payload[0] == 0x16 && payload.len() > 5 {
            // crude fingerprint extraction
            let fingerprint = Self::calculate_tls_fingerprint(payload);
            if Self::is_malicious_fingerprint(&fingerprint) {
                return Some(1); // MALICIOUS_TLS
            }
        }

        None
    }

    fn inspect_dns(&self, packet: &[u8]) -> u8 {
        // Very simple: locate DNS payload after UDP header
        // IP header length
        let ihl = (packet[0] & 0x0F) as usize * 4;
        let udp_offset = ihl;
        if packet.len() <= udp_offset + 8 { return 0; }

        let dns_offset = udp_offset + 8;
        if packet.len() <= dns_offset { return 0; }

        let query = &packet[dns_offset..];
        // If query is very long or contains base64-like content, mark as tunneling
        if query.len() > 100 || Self::looks_like_base64(query) {
            return 3; // DNS_TUNNELING
        }

        0
    }

    // --- helpers ---
    fn is_known_c2_port(port: u16) -> bool {
        matches!(port, 8080 | 8443 | 53 | 5353 | 1935 | 9999 | 22 | 23)
    }

    fn bytes_contains_case_insensitive(hay: &[u8], needle: &[u8]) -> bool {
        if needle.is_empty() || hay.len() < needle.len() { return false; }
        let lower_hay: Vec<u8> = hay.iter().map(|b| b.to_ascii_lowercase()).collect();
        let lower_needle: Vec<u8> = needle.iter().map(|b| b.to_ascii_lowercase()).collect();
        lower_hay.windows(lower_needle.len()).any(|w| w == lower_needle.as_slice())
    }

    fn looks_like_base64(slice: &[u8]) -> bool {
        // check subset of bytes for base64 chars
        let sample_len = slice.len().min(128);
        let s = &slice[..sample_len];
        let mut alpha = 0usize;
        for &c in s {
            if (b'A'..=b'Z').contains(&c) || (b'a'..=b'z').contains(&c) || (b'0'..=b'9').contains(&c) || c == b'+' || c == b'/' || c == b'=' || c == b'-' || c == b'_' {
                alpha += 1;
            }
        }
        alpha * 100 / sample_len > 90 // >90% base64-like chars
    }

    fn calculate_tls_fingerprint(payload: &[u8]) -> String {
        // Very simplified pseudo-JA3: collect first bytes of ClientHello extensions
        // This is a placeholder: real parsing requires TLS parsing
        let mut parts: Vec<String> = Vec::new();
        for i in 0..payload.len().min(32) {
            parts.push(format!("{}", payload[i]));
        }
        parts.join("-")
    }

    fn is_malicious_fingerprint(fp: &str) -> bool {
        // Compare against a small set of known bad substrings (simplified)
        const MAL_FPS: [&str; 2] = [
            "771-52393-52392-49195-49199",
            "771-49195-49199-49196-49200",
        ];
        MAL_FPS.iter().any(|m| fp.contains(m))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_base64_detector() {
        let s = b"c29tZWJhc2U2NHN0cmluZw==";
        assert!(PacketInspector::looks_like_base64(s));
    }
}
