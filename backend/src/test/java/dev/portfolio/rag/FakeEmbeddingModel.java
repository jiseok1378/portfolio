package dev.portfolio.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.ArrayList;
import java.util.List;

/** 글자 길이로 만든 3차원 벡터를 돌려주는 테스트용 임베딩 모델. */
class FakeEmbeddingModel implements EmbeddingModel {

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> out = new ArrayList<>();
        for (int i = 0; i < request.getInstructions().size(); i++) {
            out.add(new Embedding(vector(request.getInstructions().get(i)), i));
        }
        return new EmbeddingResponse(out);
    }

    @Override
    public float[] embed(Document document) {
        return vector(document.getText());
    }

    @Override
    public int dimensions() {
        return 3;
    }

    private static float[] vector(String text) {
        return new float[] {text.length(), text.length() % 7, 1f};
    }
}
