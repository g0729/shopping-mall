package com.shopping.main.domain.user.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.main.domain.user.dto.UserRegisterDto;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        // given
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("test@test.com")
                .password("password123")
                .passwordCheck("password123")
                .nickname("테스터")
                .build();

        // when
        Long userId = userService.register(dto);

        // then
        assertThat(userId).isNotNull();

        SiteUser savedUser = userRepository.findById(userId).orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");
        assertThat(savedUser.getProvider()).isEqualTo("local");
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void register_fail_password_mismatch() {
        // given
        UserRegisterDto dto = UserRegisterDto.builder()
                .email("test@test.com")
                .password("password123")
                .passwordCheck("differentPassword")
                .nickname("테스터")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호와 확인용 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_fail_duplicate_email() {
        // given
        UserRegisterDto firstDto = UserRegisterDto.builder()
                .email("duplicate@test.com")
                .password("password123")
                .passwordCheck("password123")
                .nickname("첫번째")
                .build();

        UserRegisterDto secondDto = UserRegisterDto.builder()
                .email("duplicate@test.com")
                .password("password456")
                .passwordCheck("password456")
                .nickname("두번째")
                .build();

        // when
        userService.register(firstDto);

        // then
        assertThatThrownBy(() -> userService.register(secondDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
}