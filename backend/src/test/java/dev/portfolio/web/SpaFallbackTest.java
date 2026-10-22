package dev.portfolio.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 화면 주소(/cases/...)로 바로 들어오거나 새로고침해도 index.html 을 돌려줘야 한다. */
@SpringBootTest(properties = "portfolio.vector-store-path=file:build/does-not-exist/vector-store.json")
@AutoConfigureMockMvc
class SpaFallbackTest {

    @Autowired
    MockMvc mvc;

    @Test
    void casePath_returnsIndexHtml() throws Exception {
        mvc.perform(get("/cases/realtime-records"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SPA_INDEX")));
    }

    @Test
    void unknownPagePath_returnsIndexHtml_forClientRouting() throws Exception {
        mvc.perform(get("/anything/deep"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SPA_INDEX")));
    }

    @Test
    void root_forwardsToIndexHtml() throws Exception {
        // 루트는 Spring 의 welcome page 가 index.html 로 forward 한다(MockMvc 는 forward 본문을 그리지 않음)
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void staticAsset_isServedAsIs() throws Exception {
        mvc.perform(get("/assets/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("asset")));
    }

    @Test
    void missingAsset_is404_notIndexHtml() throws Exception {
        mvc.perform(get("/assets/missing.js"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownApi_is404_notIndexHtml() throws Exception {
        mvc.perform(get("/api/nope"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("SPA_INDEX"))));
    }
}
