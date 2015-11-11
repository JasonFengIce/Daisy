package cn.ismartv.activator.data;

public class Erro {
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return getMsg();
    }
}
