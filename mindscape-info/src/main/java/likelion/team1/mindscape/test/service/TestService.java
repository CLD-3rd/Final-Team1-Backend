package likelion.team1.mindscape.test.service;

import jakarta.transaction.Transactional;
import likelion.team1.mindscape.test.dto.TestRequestDto;
import likelion.team1.mindscape.test.dto.TestResponseDto;
import likelion.team1.mindscape.test.dto.TestResponseSimpleDto;
import likelion.team1.mindscape.test.entity.Test;
import likelion.team1.mindscape.test.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;

    @Transactional
    public Long saveTest(TestRequestDto dto) {
        Test test = Test.builder()
                .userId(dto.getUserId())
                .userType(dto.getUserType())
                .typeDescription(dto.getTypeDescription())
                .build();



        return testRepository.save(test).getTestId();
    }

    public List<TestResponseDto> getTestHistory(Long userId) {
        List<Test> testList = testRepository.findByUserIdOrderByCreatedAtDesc(userId);

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
                .userId(test.getUserId())  // ✅ 객체 대신 userId 직접 반환
                .userType(test.getUserType())
                .build();
    }
    public List<Long> getTestIdsByUserId(Long userId, Pageable pageable) {
        Page<Long> page = testRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(Test::getTestId);
        List<Long> res = page.stream().toList();
        return res;
    }

}