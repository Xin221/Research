/**
 *
 */
package inetintelliprocess.bean;

import inetintelliprocess.dbc.DBConnect;

import java.sql.SQLException;

/**
 * @author Xin
 *
 */
public class UserInfo {
    private String uid;//用户名
    private String name;//真实姓名
    private String email;//邮箱地址
    private String password;//密码
    private String passwordHint;//
    private String sex;//性别
    private String birthday;//生日
    private String portrait;//头像完整地址
    private String cardNo;//身份证号
    private String educational;//学历
    private String school;//学校
    private String jobTitle;//职称
    private String duty;//职位
    private String telephone;//联系电话
    private String officePhone;//办公电话
    private String postcode;//邮编
    private String address;//家庭住址
    private String hometown;//家乡
    private String department;//所属部门
    private String filiale;//所属分公司
    private String createDate;//创建日期
    private String lockout;//锁定/逻辑删除

    public UserInfo(String uid,String email){
        this.uid = uid;
        this.email = email;
    }

    public boolean insert(UserInfo user,String tblName){
        boolean flag = false;
        if(user.isExist(tblName,user))
            return true;

        return flag;
    }



    public boolean update(UserInfo user,String tblName){
        boolean flag = false;

        return flag;
    }

    public boolean delete(String userId,String tblName){
        boolean flag = false;

        return flag;
    }


    public String getUid() {
        return uid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getEducational() {
        return educational;
    }

    public void setEducational(String educational) {
        this.educational = educational;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHometown() {
        return hometown;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFiliale() {
        return filiale;
    }

    public void setFiliale(String filiale) {
        this.filiale = filiale;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getLockout() {
        return lockout;
    }

    public void setLockout(String lockout) {
        this.lockout = lockout;
    }

    private synchronized boolean isExist(String tname,UserInfo user) {
        boolean flag = false;
        String sql = "SELECT count(*) FROM " + tname + " WHERE uid='" + user.getUid()+"'";
        //如果执行失败，需要处理异常
        try {
            if(DBConnect.stat(sql)>0)
                flag = true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return flag;
    }
    public void printUser(){
        System.out.println("uid : "+this.getUid());
        System.out.println("name : "+this.getName());
        System.out.println("email : "+this.getEmail());
        System.out.println("password : "+this.getPassword());
        System.out.println("passwordhint : "+this.getPasswordHint());
        System.out.println("sex : "+this.getSex());
        System.out.println("birthday : "+this.getBirthday());
        System.out.println("portrait : "+this.getPortrait());
        System.out.println("cardNo : "+this.getCardNo());
        System.out.println("educational : "+this.getEducational());
        System.out.println("school : "+this.getSchool());
        System.out.println("jobTitle : "+this.getJobTitle());
        System.out.println("duty : "+this.getDuty());
        System.out.println("telephone : "+this.getTelephone());
        System.out.println("officePhone : "+this.getOfficePhone());
        System.out.println("postcode : "+this.getPostcode());
        System.out.println("address : "+this.getAddress());
        System.out.println("hometown : "+this.getHometown());
        System.out.println("department : "+this.getDepartment());
        System.out.println("filiale : "+this.getFiliale());
        System.out.println("createDate : "+this.getCreateDate());
        System.out.println("lockout : "+this.getLockout());

    }

}
