package tv.ismar.daisy.data.http.newvip.paylayer;

/**
 * Created by huaijie on 4/11/16.
 */
public class PayLayerEntity {
    private int pk;

    private int pay_type;

    private String cpname;

    private int cpid;

    private Vip vip;

    private Expense_item expense_item;

    private Package pkage;

    public void setPk(int pk) {
        this.pk = pk;
    }

    public int getPk() {
        return this.pk;
    }

    public Package getPkage() {
        return pkage;
    }

    public void setPkage(Package pkage) {
        this.pkage = pkage;
    }

    public void setPay_type(int pay_type) {
        this.pay_type = pay_type;
    }

    public int getPay_type() {
        return this.pay_type;
    }

    public void setCpname(String cpname) {
        this.cpname = cpname;
    }

    public String getCpname() {
        return this.cpname;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public int getCpid() {
        return this.cpid;
    }

    public void setVip(Vip vip) {
        this.vip = vip;
    }

    public Vip getVip() {
        return this.vip;
    }

    public void setExpense_item(Expense_item expense_item) {
        this.expense_item = expense_item;
    }

    public Expense_item getExpense_item() {
        return this.expense_item;
    }

}
