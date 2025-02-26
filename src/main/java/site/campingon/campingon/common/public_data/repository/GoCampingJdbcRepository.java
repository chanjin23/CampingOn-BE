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
    public void bulkInsertCamp(List<GoCampingParsedResponseDto> responseDtoList) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            int cnt = 0;
            try (PreparedStatement ps = connection.prepareStatement(INSERT_CAMP_SQL)) {
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
                log.info("총 {}개의 camp 데이터가 삽입되었습니다.", cnt);
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
    public void bulkInsertCampInfo(List<GoCampingParsedResponseDto> responseDtoList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            int cnt = 0;
            try (PreparedStatement ps = connection.prepareStatement(INSERT_CAMP_INFO_SQL)) {
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
                log.info("총 {}개의 camp_info 데이터가 삽입되었습니다.", cnt);
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

    /**
     * campAddr*/
    public void bulkInsertCampAddr(List<GoCampingParsedResponseDto> responseDtoList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작

            try (PreparedStatement ps = connection.prepareStatement(INSERT_CAMP_ADDR_SQL)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    ps.setLong(1, data.getContentId());   // camp_id
                    ps.setString(2, data.getDoNm());   // city
                    ps.setString(3, data.getSigunguNm());  // state
                    ps.setString(4, data.getZipcode()); // zipcode
                    ps.setString(5, data.getAddr1()); // street_addr
                    ps.setString(6, data.getAddr2()); // detailed_addr
                    ps.setString(7, "POINT(" + data.getMapY() + " " + data.getMapX() + ")"); // location (WKT 형식)

                    ps.addBatch(); // 배치 추가
                }

                int[] row = ps.executeBatch();

                connection.commit(); // 트랜잭션 성공 시 커밋
                log.info("총 {}개의 camp_addr 데이터가 삽입되었습니다.", row.length);
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
    public void bulkInsertCampSite(List<GoCampingParsedResponseDto> responseDtoList) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false); // 트랜잭션 시작
            int cnt = 0;
            int dataCnt = 0;
            try (PreparedStatement ps = connection.prepareStatement(INSERT_CAMP_SITE_SQL)) {
                for (GoCampingParsedResponseDto data : responseDtoList) {
                    //일반
                    for (int i = 0; i < data.getGnrlSiteCo(); ++i) {
                        ps.setLong(1, data.getContentId());   // camp_id
                        ps.setInt(2, NORMAL_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(3, NORMAL_SITE.getPrice());  // price
                        ps.setString(4, NORMAL_SITE.getType()); // site_type
                        ps.setString(5, null); // indoor_facility
                        ps.setBoolean(6, true); // is_available
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //자동차
                    for (int i = 0; i < data.getGnrlSiteCo(); ++i) {
                        ps.setLong(1, data.getContentId());   // camp_id
                        ps.setInt(2, CAR_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(3, CAR_SITE.getPrice());  // price
                        ps.setString(4, CAR_SITE.getType()); // site_type
                        ps.setString(5, null); // indoor_facility
                        ps.setBoolean(6, true); // is_available
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //글램핑
                    for (int i = 0; i < data.getGlampSiteCo(); ++i) {
                        ps.setLong(1, data.getContentId());   // camp_id
                        ps.setInt(2, GLAMP_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(3, GLAMP_SITE.getPrice());  // price
                        ps.setString(4, GLAMP_SITE.getType()); // site_type
                        ps.setString(5, data.getGlampInnerFclty()); // indoor_facility
                        ps.setBoolean(6, true); // is_available
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //카라반
                    for (int i = 0; i < data.getCaravSiteCo(); ++i) {
                        ps.setLong(1, data.getContentId());   // camp_id
                        ps.setInt(2, CARAV_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(3, CARAV_SITE.getPrice());  // price
                        ps.setString(4, CARAV_SITE.getType()); // site_type
                        ps.setString(5, data.getCaravInnerFclty()); // indoor_facility
                        ps.setBoolean(6, true); // is_available
                        ps.addBatch(); // 배치 추가
                        cnt++;
                        dataCnt++;
                    }
                    //개인 카라반
                    for (int i = 0; i < data.getIndvdlCaravSiteCo(); ++i) {
                        ps.setLong(1, data.getContentId());   // camp_id
                        ps.setInt(2, PERSONAL_CARAV_SITE.getMaximumPeople());   // maximum_people
                        ps.setInt(3, PERSONAL_CARAV_SITE.getPrice());  // price
                        ps.setString(4, PERSONAL_CARAV_SITE.getType()); // site_type
                        ps.setString(5, null); // indoor_facility
                        ps.setBoolean(6, true); // is_available
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
                log.info("총 {}개의 camp_site 데이터가 삽입되었습니다.", dataCnt);
            } catch (SQLException e) {
                connection.rollback(); // 실패 시 롤백
                throw new RuntimeException("Batch insert failed", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }
}
