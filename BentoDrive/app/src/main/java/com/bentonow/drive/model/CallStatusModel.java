package com.bentonow.drive.model;

public class CallStatusModel {

    private boolean error;
    private int totalCalls;
    private int strenght;
    private String cell;
    private int hour;
    private int minute;
    private int second;
    private String imei;
    private String model;
    private String succed;
    private String message;
    private String operator;
    private String mcc;
    private String mcn;
    private String cellLocation;
    private String gsmLocation;
    private String countryCode;
    private boolean conexion;
    private String tipoConexion;
    private String versionAndroid;
    private double lat;
    private double lon;
    private String resultado;


    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getVersionAndroid() {
        return versionAndroid;
    }

    public void setVersionAndroid(String versionAndroid) {
        this.versionAndroid = versionAndroid;
    }

    public boolean isConexion() {
        return conexion;
    }

    public void setConexion(boolean conexion) {
        this.conexion = conexion;
    }

    public String getTipoConexion() {
        return tipoConexion;
    }

    public void setTipoConexion(String tipoConexion) {
        this.tipoConexion = tipoConexion;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getGsmLocation() {
        return gsmLocation;
    }

    public void setGsmLocation(String gsmLocation) {
        this.gsmLocation = gsmLocation;
    }

    public String getCellLocation() {
        return cellLocation;
    }

    public void setCellLocation(String cellLocation) {
        this.cellLocation = cellLocation;
    }

    public String getMcn() {
        return mcn;
    }

    public void setMcn(String mcn) {
        this.mcn = mcn;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getSucced() {
        return succed;
    }

    public void setSucced(String succed) {
        this.succed = succed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(int totalCalls) {
        this.totalCalls = totalCalls;
    }

    public int getStrenght() {
        return strenght;
    }

    public void setStrenght(int strenght) {
        this.strenght = strenght;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
