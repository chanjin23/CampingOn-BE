package site.campingon.campingon.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import site.campingon.campingon.bookmark.entity.Bookmark;
import site.campingon.campingon.common.entity.BaseEntity;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(name = "up_email_deleted_at", columnNames = {"email", "deleted_at"})
}/*, indexes = {
        @Index(name = "idx_oauth_name", columnList = "oauthName"),
}*/)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(length = 60)
    private String password;

    @Column(length = 50)
    private String name;

    // oauth 로그인인 경우에 생성되는 값
    private String oauthName;

    @Column(nullable = false, length = 24, unique = true)
    private String nickname;

    @Column(name="delete_reason", columnDefinition = "TEXT")
    private String deleteReason;

    @Column(name = "deleted_at", columnDefinition = "DATETIME(0) DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserKeyword> keywords = new ArrayList<>();

    // 회원 탈퇴 로직
    public void deleteUser(String deleteReason) {
        this.deletedAt = LocalDateTime.now();
        this.deleteReason = deleteReason;
    }

    // 회원 닉네임 변경
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }


    // 회원 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    @Override
    public String toString() {

        return "[User(" +
            "id=" + id +
            ", email=" + email +
            ", password=" + password +
            ", name=" + name +
            ", oauthName=" + oauthName +
            ", nickname=" + nickname +
            ", deleteReason=" + deleteReason +
            ", deletedAt=" + deletedAt +
            ", role=" + role +
            ")]";
    }
}
