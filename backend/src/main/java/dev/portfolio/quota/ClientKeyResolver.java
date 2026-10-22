package dev.portfolio.quota;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 질문 수를 셀 접속자 키(IP)를 고른다.
 * 프록시는 받은 요청의 주소를 X-Forwarded-For 끝에 덧붙이므로, 클라이언트가 앞에 넣은 값은 믿지 않고
 * 우리가 둔 프록시 개수(trusted-proxy-hops)만큼 뒤에서 센 위치를 쓴다.
 * 예: Docker Compose 의 Nginx 하나 → 1, CDN + Nginx → 2.
 */
@Component
public class ClientKeyResolver {

    private final int trustedProxyHops;

    public ClientKeyResolver(@Value("${portfolio.quota.trusted-proxy-hops:1}") int trustedProxyHops) {
        this.trustedProxyHops = trustedProxyHops;
    }

    public String resolve(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (trustedProxyHops <= 0 || header == null || header.isBlank()) {
            return request.getRemoteAddr();
        }
        String[] entries = header.split(",");
        int index = Math.max(0, entries.length - trustedProxyHops);
        return entries[index].trim();
    }
}
