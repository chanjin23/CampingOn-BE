package site.campingon.campingon.common.public_data.repository;

public class GoCampingUpsertSQLConstants {
    protected static final String INSERT_CAMP_SQL = "INSERT INTO camp " +
            "(created_at, modified_at,tel, animal_admission, camp_name, homepage, intro, line_intro, outdoor_facility, thumb_image, id) " +
            "VALUES (?, ? ,? ,? ,? ,? ,? ,? ,? ,?, ?)";
    protected static final String UPDATE_CAMP_SQL = "UPDATE camp " +
            "SET created_at = ?, modified_at = ?, tel = ?, animal_admission = ?, camp_name = ?, " +
            "homepage = ?, intro = ?, line_intro = ?, outdoor_facility = ?, thumb_image = ? " +
            "WHERE id = ?";

    protected static final String INSERT_CAMP_INFO_SQL = "INSERT INTO camp_info " +
            "(recommend_cnt, bookmark_cnt, camp_id)" +
            "VALUES (?, ?, ?)";

    protected static final String UPDATE_CAMP_INFO_SQL = "UPDATE camp_info " +
            "SET recommend_cnt = ?, bookmark_cnt = ? " +
            "WHERE camp_id = ?";

    protected static final String INSERT_CAMP_INDUTY_SQL = " INSERT INTO camp_induty " +
            "(camp_id, induty)" +
            "VALUES (?, ?)";

    protected static final String DELETE_CAMP_INDUTY_SQL = "DELETE FROM camp_induty " +
            "WHERE camp_id = ?";

    protected static final String INSERT_CAMP_ADDR_SQL = "INSERT INTO camp_addr " +
            "(city, state, zipcode, street_addr, detailed_addr, location, camp_id) " +
            "VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ?)";

    protected static final String UPDATE_CAMP_ADDR_SQL = "UPDATE camp_addr " +
            "SET city = ?, state = ?, zipcode = ?, street_addr = ?, detailed_addr = ?, location = ST_GeomFromText(?, 4326) " +
            "WHERE camp_id = ?";

    protected static final String INSERT_CAMP_SITE_SQL = "INSERT INTO camp_site " +
            "(maximum_people, price, site_type, indoor_facility, is_available, camp_id) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    protected static final String UPDATE_CAMP_SITE_SQL = "UPDATE camp_site " +
            "SET maximum_people = ?, price = ?, site_type = ?, indoor_facility = ?, is_available = ? " +
            "WHERE camp_id = ?";

    public static final String INSERT_MODE = "INSERT";
    public static final String UPDATE_MODE = "UPDATE";
}
