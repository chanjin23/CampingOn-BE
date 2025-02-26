package site.campingon.campingon.common.public_data.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.campingon.campingon.camp.entity.Induty;
import site.campingon.campingon.common.public_data.dto.GoCampingParsedResponseDto;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static site.campingon.campingon.camp.entity.Induty.*;
import static site.campingon.campingon.common.public_data.repository.GoCampingUpsertSQLConstants.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GoCampingJdbcRepository {
    private final DataSource dataSource;

    /**
     * camp
     * */
    public void bulkUpsertCamp(List<GoCampingParsedResponseDto> responseDtoList, String crud) {
        String sql = "";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            int cnt = 0;

            if(crud.equals(INSERT_MODE)) sql = INSERT_CAMP_SQL;
            else sql = UPDATE_CAMP_SQL;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GoCampingParsedResponseDto response : responseDtoList) {
                    ps.setObject(1, LocalDateTime.parse(
                            response.getCreatedtime()
                            , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));    //created_at
                    ps.setObject(2, LocalDateTime.parse(
                            response.getCreatedtime()
                            , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));   //modified_at
                    ps.setString(3, response.getTel());   //tel
                    ps.setString(4, response.getAnimalCmgCl());   //animal_admission
                    ps.setString(5, response.getFacltNm());   //camp_name
                    ps.setString(6, response.getHomepage());   //homepage
                    ps.setString(7, response.getIntro());   //intro
                    ps.setString(8, response.getLineIntro());   //line_intro
                    ps.setString(9, response.getSbrsCl());   //outdoor_facility
                    ps.setString(10, response.getFirstImageUrl());   //thumb_image
                    ps.setLong(11, response.getContentId());   //thumb_id
                    cnt++;
                    ps.addBatch();
                    //memory 로 인한 초기화 작업
                    if (cnt % 1000 == 0) {
                        ps.executeBatch();
                        ps.clearBatch();
                    }
                }

                ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                if(crud.equals(INSERT_MODE)) log.info("총 {}개의 camp 데이터가 삽입되었습니다.", cnt);
                else log.info("총 {}개의 camp 데이터가 업데이트되었습니다.", cnt);
            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                throw new RuntimeException("Batch insert failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * campInfo*/
    public void bulkUpsertCampInfo(List<GoCampingParsedResponseDto> responseDtoList, String crud) {
        String sql = "";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            int cnt = 0;
            if(crud.equals(INSERT_MODE)) sql = INSERT_CAMP_INFO_SQL;
            else sql = UPDATE_CAMP_INFO_SQL;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    ps.setInt(1, 0); //recommend_cnt
                    ps.setInt(2, 0);//bookmark_cnt
                    ps.setLong(3, data.getContentId());//camp_id
                    ps.addBatch();
                    cnt++;
                    if (cnt % 1000 == 0) {
                        ps.executeBatch();
                        ps.clearBatch();
                    }
                }

                ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                if(crud.equals(INSERT_MODE)) log.info("총 {}개의 camp_info 데이터가 삽입되었습니다.", cnt);
                else log.info("총 {}개의 camp_info 데이터가 업데이트되었습니다.", cnt);
            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                throw new RuntimeException("Batch insert failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * campInduty*/
    public void bulkInsertCampInduty(List<GoCampingParsedResponseDto> responseDtoList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(INSERT_CAMP_INDUTY_SQL)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    Map<Induty, Integer> siteCounts = Map.of(
                            NORMAL_SITE, data.getGnrlSiteCo(),
                            CAR_SITE, data.getAutoSiteCo(),
                            GLAMP_SITE, data.getGlampSiteCo(),
                            CARAV_SITE, data.getCaravSiteCo(),
                            PERSONAL_CARAV_SITE, data.getIndvdlCaravSiteCo()
                    );

                    //야영지가 1이상 존재하면 Induty 테이블에 저장
                    siteCounts.forEach((induty, count) -> {
                        if (count != 0) {
                            try {
                                ps.setLong(1, data.getContentId());
                                ps.setString(2, induty.getType());
                                ps.addBatch();
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                }

                int[] row = ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                log.info("총 {}개의 camp_induty 데이터가 삽입되었습니다.", row.length);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void bulkDeleteCampInduty(List<GoCampingParsedResponseDto> responseDtoList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            try (PreparedStatement ps = connection.prepareStatement(DELETE_CAMP_INDUTY_SQL)) {
                int count = 0;

                for (GoCampingParsedResponseDto data : responseDtoList) {
                    ps.setLong(1, data.getContentId());
                    ps.addBatch();
                    count++;

                    // 메모리 절약을 위해 일정 개수마다 실행
                    if (count % 1000 == 0) {
                        ps.executeBatch();
                        ps.clearBatch();
                    }
                }

                // 남은 배치 실행
                int[] row = ps.executeBatch();
                connection.commit(); // 트랜잭션 커밋
                log.info("총 {}개 camp_id의 camp_induty 데이터가 삭제되었습니다.", Arrays.stream(row).sum());

            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                log.error("캠핑업종 삭제 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("Batch delete failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }


    /**
     * campAddr*/
    public void bulkUpsertCampAddr(List<GoCampingParsedResponseDto> responseDtoList, String crud) {
        String sql = "";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            if(crud.equals(INSERT_MODE)) sql = INSERT_CAMP_ADDR_SQL;
            else sql = UPDATE_CAMP_ADDR_SQL;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    String pointWKT = String.format("POINT(%.6f %.6f)", data.getMapY(), data.getMapX());

                    ps.setString(1, data.getDoNm());   // city
                    ps.setString(2, data.getSigunguNm());  // state
                    ps.setString(3, data.getZipcode()); // zipcode
                    ps.setString(4, data.getAddr1()); // street_addr
                    ps.setString(5, data.getAddr2()); // detailed_addr
                    ps.setString(6, pointWKT); // location (WKT 형식) -> '' 표시가 될수있으므로 Object 설정
                    ps.setLong(7, data.getContentId());   // camp_id

                    ps.addBatch(); // 배치 추가
                }

                int[] row = ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                if(crud.equals(INSERT_MODE)) log.info("총 {}개의 camp_addr 데이터가 삽입되었습니다.", row.length);
                else log.info("총 {}개의 camp_addr 데이터가 업데이트되었습니다.", row.length);
            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                throw new RuntimeException("Batch insert failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * campSite*/
    public void bulkUpsertCampSite(List<GoCampingParsedResponseDto> responseDtoList, String crud) {
        String sql = "";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작
            int cnt = 0;
            int dataCnt = 0;

            if(crud.equals(INSERT_MODE)) sql = INSERT_CAMP_SITE_SQL;
            else sql = UPDATE_CAMP_SITE_SQL;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    //일반
                    for (int i = 0; i < data.getGnrlSiteCo(); ++i) {
                        ps.setInt(1, NORMAL_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(2, NORMAL_SITE.getPrice());  // price
                        ps.setString(3, NORMAL_SITE.getType()); // site_type
                        ps.setString(4, null); // indoor_facility
                        ps.setBoolean(5, true); // is_available
                        ps.setLong(6, data.getContentId());   // camp_id
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //자동차
                    for (int i = 0; i < data.getGnrlSiteCo(); ++i) {
                        ps.setInt(1, CAR_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(2, CAR_SITE.getPrice());  // price
                        ps.setString(3, CAR_SITE.getType()); // site_type
                        ps.setString(4, null); // indoor_facility
                        ps.setBoolean(5, true); // is_available
                        ps.setLong(6, data.getContentId());   // camp_id
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //글램핑
                    for (int i = 0; i < data.getGlampSiteCo(); ++i) {
                        ps.setInt(1, GLAMP_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(2, GLAMP_SITE.getPrice());  // price
                        ps.setString(3, GLAMP_SITE.getType()); // site_type
                        ps.setString(4, data.getGlampInnerFclty()); // indoor_facility
                        ps.setBoolean(5, true); // is_available
                        ps.setLong(6, data.getContentId());   // camp_id
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //카라반
                    for (int i = 0; i < data.getCaravSiteCo(); ++i) {
                        ps.setInt(1, CARAV_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(2, CARAV_SITE.getPrice());  // price
                        ps.setString(3, CARAV_SITE.getType()); // site_type
                        ps.setString(4, data.getCaravInnerFclty()); // indoor_facility
                        ps.setBoolean(5, true); // is_available
                        ps.setLong(6, data.getContentId());   // camp_id
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //개인 카라반
                    for (int i = 0; i < data.getIndvdlCaravSiteCo(); ++i) {
                        ps.setInt(1, PERSONAL_CARAV_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(2, PERSONAL_CARAV_SITE.getPrice());  // price
                        ps.setString(3, PERSONAL_CARAV_SITE.getType()); // site_type
                        ps.setString(4, null); // indoor_facility
                        ps.setBoolean(5, true); // is_available
                        ps.setLong(6, data.getContentId());   // camp_id
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    if (cnt >= 1000) {
                        ps.executeBatch();
                        ps.clearBatch();
                        cnt = 0;
                    }
                }

                ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                if(crud.equals(INSERT_MODE)) log.info("총 {}개의 camp_site 데이터가 삽입되었습니다.", dataCnt);
                else log.info("총 {}개의 camp_site 데이터가 업데이트되었습니다.", dataCnt);
            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                throw new RuntimeException("Batch insert failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }
}
