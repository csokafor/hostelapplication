/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.payment;


import com.perblo.hostel.entity.EligibleBallotStudent;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import com.perblo.security.LoginUserBean;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import javax.persistence.Query;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.model.UploadedFile;
import org.springframework.security.access.annotation.Secured;


/**
 *
 * @author chinedu
 */
@ManagedBean(name="pinAdminBean")
@SessionScoped
public class PinAdminBean implements Serializable {
    private static final Logger log = Logger.getLogger(PinAdminBean.class);
    private static HSSFDataFormatter formatter =  new HSSFDataFormatter();

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{paymentService}")
    private PaymentService paymentService;
    
    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;
        
    List<Pin> pinList = null;
    
    private Pin pin;    
    private String serialNumber;
        
    private byte[] data;     
    private String fileExtension;    
    private String fileName;    
    private String contentType;

    private Part uploadedPinFile;
        
    public PinAdminBean() {

    }
                    
    @Secured("Hostel Admin")
    public void checkPinStatus() {
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getPinBySerialNumber");
            query.setParameter(1, serialNumber);
            pin = (Pin)query.getSingleResult();
            
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Could not get pin information for serial number " + serialNumber,""));
            
            log.error("Exception in checkPinStatus: " + e.getMessage());
        }
    }

    @Secured("Pin Admin")
    public void uploadFile() {
        log.info("uploadFile");
        pinList = new ArrayList<Pin>();

        String fileName = getFilename(uploadedPinFile);
        log.info("filename: " + fileName + " , contentType: " + uploadedPinFile.getContentType() + " , size: " + uploadedPinFile.getSize());

        if(fileName.contains("xlsx") || fileName.contains("xls")) {
            getPinData(fileName);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, pinList.size() + " pin numbers uploaded",""));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.",""));
        }

    }

    private void getPinData(String fileName) {
                
        String batchNumber;
        String serialNumber;        
        String pinNumber;
        float pinValue;
        
        log.info("getPinData()");
        int numberOfRecords = 0;

        try {
                //check for xlsx files
                if (fileName.contains("xlsx")) {
                    XSSFWorkbook workBook = new XSSFWorkbook(uploadedPinFile.getInputStream());
                    XSSFSheet workSheet = workBook.getSheetAt(0);

                    int noOfRows = workSheet.getPhysicalNumberOfRows();
                    log.info("no of rows " + workSheet.getPhysicalNumberOfRows());

                    for(int i = 1; i < noOfRows; i++) {
                        Row row = workSheet.getRow(i);
                        int noOfCells = row.getPhysicalNumberOfCells();
                        log.info("upload row " + i + " no of cells " + noOfCells);

                        Cell batchNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                        batchNumber = formatter.formatCellValue(batchNumberCell).trim().toUpperCase();

                        Cell serialNumberCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                        serialNumber = formatter.formatCellValue(serialNumberCell).trim().toUpperCase();

                        Cell pinNumberCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                        pinNumber = formatter.formatCellValue(pinNumberCell).trim().toUpperCase();

                        Cell pinValueCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                        pinValue = new Double(pinValueCell.getNumericCellValue()).floatValue();

                        numberOfRecords++;
                        log.info("batchNumber = " + batchNumber + ", serialNumber = " + serialNumber + " pinValue = " + pinValue);

                        Pin oldpin = paymentService.getPinByPinNumber(pinNumber);
                        if(oldpin == null) {
                            Pin newpin = paymentService.addPin(batchNumber, serialNumber, pinNumber, pinValue, loginUserBean.getUser().getUserName());
                            pinList.add(newpin);
                        } else {
                            log.warn(serialNumber + " pin already exists");
                        }
                    }

                } else {
                    HSSFWorkbook hssfWorkBook = new HSSFWorkbook(uploadedPinFile.getInputStream());
                    HSSFSheet hssfWorkSheet = hssfWorkBook.getSheetAt(0);
                    int noOfRows = hssfWorkSheet.getPhysicalNumberOfRows();
                    log.info("no of rows " + hssfWorkSheet.getPhysicalNumberOfRows());

                    for(int i = 1; i < noOfRows; i++) {
                        Row row = hssfWorkSheet.getRow(i);
                        int noOfCells = row.getPhysicalNumberOfCells();
                        log.info("upload row " + i + " no of cells " + noOfCells);

                        Cell batchNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                        batchNumber = batchNumberCell.getStringCellValue().trim().toUpperCase();

                        Cell serialNumberCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                        serialNumber = serialNumberCell.getStringCellValue().trim().toUpperCase();

                        Cell pinNumberCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                        pinNumber = pinNumberCell.getStringCellValue().trim().toUpperCase();

                        Cell pinValueCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                        pinValue = new Double(pinValueCell.getNumericCellValue()).floatValue();

                        numberOfRecords++;
                        log.info("batchNumber = " + batchNumber + ", serialNumber = " + serialNumber + " pinValue = " + pinValue);

                        Pin newpin = paymentService.addPin(batchNumber, serialNumber, pinNumber, pinValue, loginUserBean.getUser().getUserName());
                        pinList.add(newpin);
                    }
                }

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.","")); 
            
        }
    }

    public void validateExcelFile(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        Part file = (Part) value;
        String contentType = file.getContentType();
        log.info("contentType: " + contentType);

        String fileName = getFilename(file);
        if (!(fileName.contains("xls") || fileName.contains("xlsx"))) {

            msgs.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid File",
                    fileName + " is not a valid excel file"));
        }
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);

            }
        }
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Pin getPin() {
        return pin;
    }

    public void setPin(Pin pin) {
        this.pin = pin;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public Part getUploadedPinFile() {
        return uploadedPinFile;
    }

    public void setUploadedPinFile(Part uploadedPinFile) {
        this.uploadedPinFile = uploadedPinFile;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }
}
