package site.campingon.campingon.common.config;

public class SecurityPath {

    // permitAll
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/signup",
        "/api/login",
        "/api/token/refresh",
        "/api/users/check-duplicate",
        "/",
        "/api/mongo/camps/search",
        "/api/mongo/camps/autocomplete",
        "/api/camps/*/available",
        "/api/camps/*",
        "/api/camps/*/reviews",  // 캠핑장 Id로 리뷰 목록 조회
        "/api/camps/reviews/*"
    };


    // hasRole("USER")
    public static final String[] USER_ENDPOINTS = {
        "/api/mongo/camps/matched",
        "/api/users/me/bookmarked",
        "/api/users/me/*",
        "/api/users/me",
        "/api/reservations/**",
        "/api/camps/*/bookmarks",
        "/api/logout",
        "/api/keywords",
        "/api/keywords/me",
        "/api/camps/bookmarked",
        "/api/camps/*/reviews/*"  // 리뷰 생성
    };

    // hasRole("ADMIN")
    public static final String[] ADMIN_ENDPOINTS = {
        "/api/admin/**",
        "/api/basedList/**",
        "/api/imageList/**"

    };
}
