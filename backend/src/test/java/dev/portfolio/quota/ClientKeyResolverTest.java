package dev.portfolio.quota;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientKeyResolverTest {

    @Test
    void noHeader_usesRemoteAddr() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.9");
        assertThat(new ClientKeyResolver(1).resolve(req)).isEqualTo("10.0.0.9");
    }

    @Test
    void oneTrustedProxy_usesLastEntry_ignoringSpoofedPrefix() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("172.18.0.2"); // 우리 프록시 컨테이너
        req.addHeader("X-Forwarded-For", "1.1.1.1, 203.0.113.7"); // 1.1.1.1 은 클라이언트가 보낸 위조 값
        assertThat(new ClientKeyResolver(1).resolve(req)).isEqualTo("203.0.113.7");
    }

    @Test
    void twoTrustedProxies_usesSecondFromLast() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("172.18.0.2");
        req.addHeader("X-Forwarded-For", "1.1.1.1, 203.0.113.7, 130.176.0.10"); // CDN → 우리 프록시
        assertThat(new ClientKeyResolver(2).resolve(req)).isEqualTo("203.0.113.7");
    }

    @Test
    void fewerEntriesThanHops_usesFirstEntry() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("172.18.0.2");
        req.addHeader("X-Forwarded-For", "203.0.113.7");
        assertThat(new ClientKeyResolver(2).resolve(req)).isEqualTo("203.0.113.7");
    }

    @Test
    void zeroTrustedProxies_ignoresHeader() {
        var req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.9");
        req.addHeader("X-Forwarded-For", "1.1.1.1");
        assertThat(new ClientKeyResolver(0).resolve(req)).isEqualTo("10.0.0.9");
    }
}
