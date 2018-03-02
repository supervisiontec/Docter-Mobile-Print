package lk.supervision.doctermaster.model;

/**
 * Created by kavish manjitha on 12/18/2017.
 */

public class MSettings {

    private Integer indexNo;
    private String bluetoothPrinter;
    private String bluetoothPrinterMac;
    private String centerName;
    private String centerAddress;
    private String centerContactNo;
    private String footer;

    public MSettings() {
    }

    public Integer getIndexNo() {
        return indexNo;
    }

    public void setIndexNo(Integer indexNo) {
        this.indexNo = indexNo;
    }

    public String getBluetoothPrinter() {
        return bluetoothPrinter;
    }

    public void setBluetoothPrinter(String bluetoothPrinter) {
        this.bluetoothPrinter = bluetoothPrinter;
    }

    public String getBluetoothPrinterMac() {
        return bluetoothPrinterMac;
    }

    public void setBluetoothPrinterMac(String bluetoothPrinterMac) {
        this.bluetoothPrinterMac = bluetoothPrinterMac;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getCenterAddress() {
        return centerAddress;
    }

    public void setCenterAddress(String centerAddress) {
        this.centerAddress = centerAddress;
    }

    public String getCenterContactNo() {
        return centerContactNo;
    }

    public void setCenterContactNo(String centerContactNo) {
        this.centerContactNo = centerContactNo;
    }
}
