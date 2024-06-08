package model;

/**
 * Represents the user of the app.
 */
public class UserData {
    private String first_name;
    private String middle_name;
    private String last_name;
    private String suffix_name;
    private String id_number;
    private String year_level;
    private String program_code;
    private String position;
    private String ec_code;
    private String society_code;
    private String org_code; // for like BUFICOM Head that only have 1 organization

    /**
     * If the user is a class rep or with multiple organization.
     *
     * @param first_name
     * @param middle_name
     * @param last_name
     * @param suffix_name
     * @param id_number
     * @param year_level
     * @param program_code
     * @param position
     * @param ec_code
     * @param society_code
     */
    public UserData(String first_name, String middle_name, String last_name, String suffix_name, String id_number,
            String year_level,
            String program_code, String position, String ec_code, String society_code) {
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.suffix_name = suffix_name;
        this.id_number = id_number;
        this.year_level = year_level;
        this.program_code = program_code;
        this.position = position;
        this.ec_code = ec_code;
        this.society_code = society_code;
    }

    /*
     * Setters and Getters.
     */

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getSuffix_name() {
        return suffix_name;
    }

    public void setSuffix_name(String suffix_name) {
        this.suffix_name = suffix_name;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    public String getYear_level() {
        return year_level;
    }

    public void setYear_level(String year_level) {
        this.year_level = year_level;
    }

    public String getProgram_code() {
        return program_code;
    }

    public void setProgram_code(String program_code) {
        this.program_code = program_code;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEc_code() {
        return ec_code;
    }

    public void setEc_code(String ec_code) {
        this.ec_code = ec_code;
    }

    public String getSociety_code() {
        return society_code;
    }

    public void setSociety_code(String society_code) {
        this.society_code = society_code;
    }

    public String getOrg_code() {
        return org_code;
    }

    public void setOrg_code(String org_code) {
        this.org_code = org_code;
    }
}