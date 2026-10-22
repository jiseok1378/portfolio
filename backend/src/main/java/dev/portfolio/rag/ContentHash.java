package dev.portfolio.rag;

import org.springframework.ai.document.Document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/** RAG 문서 본문과 메타데이터로 만든 지문. 임베딩 파일이 어떤 콘텐츠로 만들어졌는지 확인하는 데 쓴다. */
public final class ContentHash {

    private ContentHash() {
    }

    public static String of(List<Document> docs) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Document doc : docs) {
                digest.update(String.valueOf(doc.getMetadata().get("slug")).getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(doc.getText().getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
