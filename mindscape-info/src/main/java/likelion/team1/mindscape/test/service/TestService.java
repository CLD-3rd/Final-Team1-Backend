package likelion.team1.mindscape.test.service;

import likelion.team1.mindscape.test.dto.TestRequestDto;
import likelion.team1.mindscape.test.entity.Test;
import likelion.team1.mindscape.test.repository.TestRepository;
import likelion.team1.mindscape.user.entity.User;
import likelion.team1.mindscape.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import likelion.team1.mindscape.test.dto.TestResponseDto;
import likelion.team1.mindscape.test.dto.TestResponseSimpleDto;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final UserRepository userRepository;

    public Long saveTest(TestRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Test test = Test.builder()
                .user(user)
                .userType(dto.getUserType())
                .typeDescription(dto.getTypeDescription())
                .build();

        Test saved = testRepository.save(test);
        return saved.getTestId();
    }

    public List<TestResponseDto> getTestHistory(Long userId) {
        List<Test> testList = testRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        return testList.stream()
                .map(test -> TestResponseDto.builder()
                        .testId(test.getTestId())
                        .userType(test.getUserType())
                        .typeDescription(test.getTypeDescription())
                        .createdAt(test.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    public TestResponseSimpleDto getTestInfoById(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("테스트 결과를 찾을 수 없습니다."));

        return TestResponseSimpleDto.builder()
                .testId(test.getTestId())
                .userId(test.getUser().getUserId())
                .userType(test.getUserType())
                .build();
    }
}
