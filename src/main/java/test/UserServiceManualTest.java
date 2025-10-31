package test;

import jdbc.domain.user.model.User;
import jdbc.domain.user.model.UserRole;
import jdbc.domain.user.service.UserService;

import java.io.IOException;
import java.util.UUID;

public class UserServiceManualTest {

    public static void main(String[] args) throws IOException {
        System.out.println("=== UserService Simple CRUD Test ===");

        UserService service = new UserService(null);

        // 1. 리스트 조회
        System.out.println("[1] 전체 리스트 조회");
        service.getAllInPage(10, 0)
                .content()
                .forEach(u -> System.out.println(" - " + u.getUserId() + " | " + u.getEmail()));

        // 2. 단건 조회 (예시로 첫 번째 ID 사용)
        System.out.println("\n[2] 단건 조회");
        var page = service.getAllInPage(1, 0);
        if (page.content().isEmpty()) {
            System.out.println("조회할 데이터가 없습니다.");
            return;
        }
        Long firstId = page.content().get(0).getUserId();
        User one = service.getById(firstId);
        System.out.println("조회 결과: " + one.getUserId() + " | " + one.getName());

        // 3. 단건 삽입
        System.out.println("\n[3] 단건 삽입");
        String email = "simple_test_" + UUID.randomUUID() + "@example.com";
        User newUser = new User(null, email, "테스트유저", "pw", UserRole.USER, null, null);
        int insert = service.create(newUser);
        System.out.println("삽입 결과 행수: " + insert);
        System.out.println("생성된 user_id: " + newUser.getUserId());

        // 4. 삽입된 단건 조회
        System.out.println("\n[4] 삽입된 단건 조회");
        User inserted = service.getById(newUser.getUserId());
        System.out.println("조회 결과: " + inserted.getUserId() + " | " + inserted.getEmail() + " | " + inserted.getName());

        // 5. 단건 업데이트 (직접 SQL 없음 → role만 변경 예시로 보여줌)
        System.out.println("\n[5] 단건 업데이트 (role 변경)");
        inserted = new User(
                inserted.getUserId(),
                inserted.getEmail(),
                inserted.getName(),
                inserted.getPassword(),
                UserRole.ADMIN, // 변경
                inserted.getCreatedAt(),
                inserted.getDeletedAt()
        );
        // MyBatis XML에 updateUser 문이 있다고 가정 (없다면 직접 SQL 호출해도 됨)
        // service.update(inserted);
        System.out.println("→ 업데이트 SQL 구현되어 있지 않다면 생략");

        // 6. 단건 재조회
        System.out.println("\n[6] 업데이트 후 재조회");
        User afterUpdate = service.getById(inserted.getUserId());
        System.out.println("조회 결과: " + afterUpdate.getUserId() + " | role=" + afterUpdate.getRole());

        // 7. 단건 삭제
        System.out.println("\n[7] 단건 삭제");
        int deleted = service.delete(inserted.getUserId());
        System.out.println("삭제 결과 행수: " + deleted);

        // 8. 삭제 후 조회
        System.out.println("\n[8] 삭제 후 재조회");
        User afterDelete = service.getById(inserted.getUserId());
        System.out.println(afterDelete == null ? "삭제 성공 (조회 결과 없음)" : "삭제 실패 (데이터 존재)");

        System.out.println("\n=== 테스트 완료 ===");
    }
}
