package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ApiV1AdmPostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("count")
    void t1() throws Exception {
        Member actor = memberService.findByUsername("admin").get();
        String actorApiKey = actor.getApiKey();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/posts/count")
                                .header("Authorization", "Bearer " + actorApiKey)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1AdmPostController.class))
                .andExpect(handler().methodName("count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.all").value(postService.count()));
    }
    @Test
    @DisplayName("count without permission ")
    void t2() throws Exception {
        // 1. 일반 유저(user2)의 API Key를 가져옵니다 (관리자 권한 없음)
        Member actor = memberService.findByUsername("user2").get();
        String actorApiKey = actor.getApiKey();

        // 2. 관리자 전용 URL(/api/v1/adm/...)에 일반 유저의 키로 요청을 보냅니다
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/adm/posts/count")
                                .header("Authorization", "Bearer " + actorApiKey)
                )
                .andDo(print());

        // 3. 403 Forbidden 에러가 발생하는지 검증합니다
        resultActions
                .andExpect(status().isForbidden()) // 403 상태 코드 확인
                .andExpect(jsonPath("$.resultCode").value("403-1")) // (선택) 예상되는 에러 코드 검증
                .andExpect(jsonPath("$.msg").exists()); // 에러 메시지가 존재하는지 확인
    }
}