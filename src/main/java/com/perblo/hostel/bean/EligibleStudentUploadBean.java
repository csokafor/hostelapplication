/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import com.perblo.hostel.entity.Hostel;
import com.perblo.hostel.entity.HostelAllocation;
import com.perblo.hostel.entity.HostelRoom;
import com.perblo.hostel.entity.HostelRoomBedSpace;
import com.perblo.hostel.entity.HostelStudentType;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationService;
import com.perblo.hostel.service.HostelConfig;
import com.perblo.hostel.service.HostelSettingsService;

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


import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

/**
 * @author chinedu
 */

@ManagedBean(name = "eligibleStudentUploadBean")
@SessionScoped
@Secured("Hostel Admin")
public class EligibleStudentUploadBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(EligibleStudentUploadBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value = "#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

    @ManagedProperty(value = "#{loginUserBean}")
    LoginUserBean loginUserBean;

    @ManagedProperty(value = "#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;

    private byte[] data;
    private String fileExtension;
    private String fileName;
    private String contentType;

    private int pageSize = 10;
    private int page;
    private String searchParam;
    private int uploadRowIndex;
    private Part uploadedEligibleFile;

    private List<EligibleStudent> eligibleStudents;

    private List<EligibleStudent> uploadedEligibleStudents;

    public EligibleStudentUploadBean() {
        uploadedEligibleStudents = new ArrayList<EligibleStudent>();
        eligibleStudents = new ArrayList<EligibleStudent>();
    }

    public void uploadFile() {
        log.info("uploadFile");
        uploadedEligibleStudents = new ArrayList<EligibleStudent>();
        eligibleStudents = new ArrayList<EligibleStudent>();
        searchParam = "";
        uploadRowIndex = 0;
        String fileName = getFilename(uploadedEligibleFile);
        log.info("filename: " + fileName + " , contentType: " + uploadedEligibleFile.getContentType() + " , size: " + uploadedEligibleFile.getSize());


        if (fileName.contains("xlsx") || fileName.contains("xls")) {
            getEligibleStudentData(fileName);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.", ""));
        }

    }

    private void getEligibleStudentData(String fileName) {

        String studentNumber;
        String studentName;
        String department;
        String studentType;
        String hostel;

        log.info("getEligibleStudentData");
        int numberOfRecords = 0;

        try {

            //check for xlsx files
            if (fileName.contains("xlsx")) {
                XSSFWorkbook workBook = new XSSFWorkbook(uploadedEligibleFile.getInputStream());
                XSSFSheet workSheet = workBook.getSheetAt(0);
                int noOfRows = workSheet.getPhysicalNumberOfRows();
                log.info("no of rows " + workSheet.getPhysicalNumberOfRows());

                for(int i = 1; i < noOfRows; i++) {
                    Row row = workSheet.getRow(i);
                    int noOfCells = row.getPhysicalNumberOfCells();
                    log.info("upload row " + i + " no of cells " + noOfCells);

                    Cell studentNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                    studentNumber = studentNumberCell.getStringCellValue().trim().toUpperCase();

                    Cell studentNameCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                    studentName = studentNameCell.getStringCellValue().trim();

                    Cell departmentCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                    department = departmentCell.getStringCellValue().trim().toUpperCase();

                    Cell studentTypeCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                    studentType = studentTypeCell.getStringCellValue().trim().toUpperCase();

                    Cell hostelCell = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
                    hostel = hostelCell.getStringCellValue().trim().toUpperCase();

                    numberOfRecords++;
                    log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department +
                            ", studentType = " + studentType + ", hostel = " + hostel);

                    EligibleStudent eliStudent = hostelApplicationService.getEligibleStudentByStudentNumber(studentNumber);
                    if (eliStudent == null) {
                        HostelStudentType hostelStudentType = hostelApplicationService.getHostelStudentType(studentType);
                        if (hostelStudentType == null) {
                            log.error("hostel student type =" + studentType + " not found");
                        } else {
                            EligibleStudent eligibleStudent = hostelApplicationService.addEligibleStudent(studentNumber, studentName, department,
                                    hostelStudentType, hostel, loginUserBean.getCurrentUser().getUserName());
                            uploadedEligibleStudents.add(eligibleStudent);
                        }
                    } else {
                        log.warn("Eligible student already exists for " + studentNumber);
                    }

                }

            } else {
                HSSFWorkbook hssfWorkBook = new HSSFWorkbook(uploadedEligibleFile.getInputStream());
                HSSFSheet hssfWorkSheet = hssfWorkBook.getSheetAt(0);
                int noOfRows = hssfWorkSheet.getPhysicalNumberOfRows();
                log.info("no of rows " + hssfWorkSheet.getPhysicalNumberOfRows());

                for(int i = 1; i < noOfRows; i++) {
                    Row row = hssfWorkSheet.getRow(i);
                    int noOfCells = row.getPhysicalNumberOfCells();
                    log.info("upload row " + i + " no of cells " + noOfCells);

                    Cell studentNumberCell = row.getCell(0, Row.RETURN_BLANK_AS_NULL);
                    studentNumber = studentNumberCell.getStringCellValue().trim().toUpperCase();

                    Cell studentNameCell = row.getCell(1, Row.RETURN_BLANK_AS_NULL);
                    studentName = studentNameCell.getStringCellValue().trim();

                    Cell departmentCell = row.getCell(2, Row.RETURN_BLANK_AS_NULL);
                    department = departmentCell.getStringCellValue().trim().toUpperCase();

                    Cell studentTypeCell = row.getCell(3, Row.RETURN_BLANK_AS_NULL);
                    studentType = studentTypeCell.getStringCellValue().trim().toUpperCase();

                    Cell hostelCell = row.getCell(4, Row.RETURN_BLANK_AS_NULL);
                    hostel = hostelCell.getStringCellValue().trim().toUpperCase();

                    numberOfRecords++;
                    log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department +
                            ", studentType = " + studentType + ", hostel = " + hostel);

                    EligibleStudent eliStudent = hostelApplicationService.getEligibleStudentByStudentNumber(studentNumber);
                    if (eliStudent == null) {
                        HostelStudentType hostelStudentType = hostelApplicationService.getHostelStudentType(studentType);
                        if (hostelStudentType == null) {
                            log.error("hostel student type =" + studentType + " not found");
                        } else {
                            EligibleStudent eligibleStudent = hostelApplicationService.addEligibleStudent(studentNumber, studentName, department,
                                    hostelStudentType, hostel, loginUserBean.getCurrentUser().getUserName());
                            uploadedEligibleStudents.add(eligibleStudent);
                        }
                    } else {
                        log.warn("Eligible student already exists for " + studentNumber);
                    }

                }
            }

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.", ""));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "An error occured while processing your request.", ""));

        } finally {

        }

    }

    public void search() {
        uploadedEligibleStudents = new ArrayList<EligibleStudent>();
        page = 0;
        queryEligibleStudent();
    }

    public void nextPage() {
        page++;
        queryEligibleStudent();
    }

    public void previousPage() {
        if (page > 0) {
            page--;
        }
        queryEligibleStudent();
    }

    private void queryEligibleStudent() {
        String queryString = "";
        Query query = null;
        if (!queryString.equalsIgnoreCase("%%")) {
            queryString = "select t from EligibleStudent t where lower(t.firstName) like '%" + searchParam +
                    "%' or lower(t.department) like '" + searchParam + "' or lower(t.studentNumber) like '" + searchParam + "'";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);

        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must enter a search parameter", ""));
        }

        //eligibleStudents = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        eligibleStudents = query.getResultList();
    }

    public boolean isNextPageAvailable() {
        return eligibleStudents != null && eligibleStudents.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }

    //@Factory(value = "eligibleStudentSearchPattern", scope = ScopeType.EVENT)
    public String getSearchPattern() {
        return searchParam == null
                ? "%" : '%' + searchParam.toLowerCase().replace('*', '%') + '%';
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

    public String getSearchParam() {
        return searchParam;
    }

    public void setSearchParam(String searchParam) {
        this.searchParam = searchParam;
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

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public List<EligibleStudent> getEligibleStudents() {
        return eligibleStudents;
    }

    public void setEligibleStudents(List<EligibleStudent> eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    public List<EligibleStudent> getUploadedEligibleStudents() {
        return uploadedEligibleStudents;
    }

    public void setUploadedEligibleStudents(List<EligibleStudent> uploadedEligibleStudents) {
        this.uploadedEligibleStudents = uploadedEligibleStudents;
    }


    public Part getUploadedEligibleFile() {
        return uploadedEligibleFile;
    }

    public void setUploadedEligibleFile(Part uploadedEligibleFile) {
        this.uploadedEligibleFile = uploadedEligibleFile;
    }

    public int getUploadRowIndex() {
        return ++uploadRowIndex;
    }

    public void setUploadRowIndex(int uploadRowIndex) {
        this.uploadRowIndex = uploadRowIndex;
    }

}
