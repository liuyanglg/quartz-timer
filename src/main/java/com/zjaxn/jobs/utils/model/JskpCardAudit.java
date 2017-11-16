package com.zjaxn.jobs.utils.model;

import com.zjaxn.jobs.utils.StringUtil;

public class JskpCardAudit {
    private Integer id;
    private String code;
    private String taxid;
    private String name;
    private String address;
    private String telephone;
    private String bank;
    private String account;
    private String source;
    private String type;
    private String note;
    private String createtime;
    private Integer status;
    private Integer cert;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTaxid() {
        return taxid;
    }

    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCert() {
        return cert;
    }

    public void setCert(Integer cert) {
        this.cert = cert;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append(StringUtil.joinStringField4Json("taxid", this.getTaxid()))
                .append(StringUtil.joinStringField4Json("name", this.getName()))
                .append(StringUtil.joinStringField4Json("address", this.getAddress()))
                .append(StringUtil.joinStringField4Json("telephone", this.getTelephone()))
                .append(StringUtil.joinStringField4Json("bank", this.getBank()))
                .append(StringUtil.joinStringField4Json("account", this.getAccount()))
                .append(StringUtil.joinStringField4Json("type", this.getType()))
                .append(StringUtil.joinIntegerField4Json("cert", this.getCert()))
                .append(StringUtil.joinStringField4Json("source", this.getSource()));
        String json = (String) builder.subSequence(0, builder.length() - 1);
        json += "}";
        return json;
    }

    @Override
    public String toString() {
        return "JskpCardAudit{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", taxid='" + taxid + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", telephone='" + telephone + '\'' +
                ", bank='" + bank + '\'' +
                ", account='" + account + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", note='" + note + '\'' +
                ", createtime='" + createtime + '\'' +
                ", status=" + status +
                ", cert=" + cert +
                '}';
    }
}
