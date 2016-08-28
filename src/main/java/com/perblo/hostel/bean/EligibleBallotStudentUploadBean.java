/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import com.perblo.hostel.entity.*;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import com.perblo.security.LoginUserBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
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

@ManagedBean(name="eligibleBallotUploadBean")
@SessionScoped
@Secured("Hostel Admin")
public class EligibleBallotStudentUploadBean implements Serializable {
    
    private static final Logger log = Logger.getLogger(EligibleBallotStudentUploadBean.class);
    private EntityManager entityManager;  
	        
    @ManagedProperty(value="#{hostelSettingsHelper}")  
    private HostelSettingsHelper hostelSettingsHelper;
    
    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;
                
    private byte[] data;     
    private String fileExtension;    
    private String fileName;    
    private String contentType;
        
    private int pageSize = 10;    
    private int page;        
    private String searchParam;
        
    private int uploadRowIndex;
    private UploadedFile uploadedFile;
    private List<EligibleBallotStudent> eligibleBallotStudents;        
    private List<EligibleBallotStudent> uploadedEligibleBallotStudents;
    
    public EligibleBallotStudentUploadBean() {
        this.entityManager = HostelEntityManagerListener.createEntityManager();   
    }
        
    public void uploadFile() {
        log.info("uploadFile");
    	uploadedEligibleBallotStudents = new ArrayList<EligibleBallotStudent>();
        eligibleBallotStudents  = new ArrayList<EligibleBallotStudent>();
        searchParam = "";
        uploadRowIndex = 0;
        log.info("filename = " + uploadedFile.getFileName() + " contentType = " + 
                uploadedFile.getContentType() + " data size = " + uploadedFile.getSize());
        
    	if(uploadedFile.getFileName().contains("xlsx") || uploadedFile.getFileName().contains("xls")) {
            getEligibleBallotStudentData();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload an excel file.",""));             
        }
            	
    }
    
    private void getEligibleBallotStudentData() {
                
        String studentNumber;
        String studentName;
        String department;
        String programme;
        String gender;
        String studentType;
        String hostel;                
        
        log.info("getEligibleBallotStudentData");
        int numberOfRecords = 0;

        try {
            
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();

            //check for xlsx files
            if (uploadedFile.getFileName().contains("xlsx")) {
                XSSFWorkbook workBook = new XSSFWorkbook(uploadedFile.getInputstream());
                XSSFSheet workSheet = workBook.getSheetAt(0);
                Iterator<Row> rowsIter = workSheet.rowIterator();
                while (rowsIter.hasNext()) {
                    Row currentRow = rowsIter.next();
                    if(currentRow.getRowNum() == 0) {
                        continue;
                    }
                    Iterator<Cell> cellsIter = currentRow.cellIterator();
                    //excel sheet format student number, student name, department, programme of study, gender
                    while (cellsIter.hasNext()) {
                        Cell studentNumberCell = cellsIter.next();
                        studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                        studentNumber = studentNumberCell.getStringCellValue();

                        Cell studentNameCell = cellsIter.next();
                        studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                        studentName = studentNameCell.getStringCellValue();

                        Cell departmentCell = cellsIter.next();
                        departmentCell.setCellType(Cell.CELL_TYPE_STRING);
                        department = departmentCell.getStringCellValue();

                                                    /*
                        Cell programmeCell = cellsIter.next();
                        programmeCell.setCellType(Cell.CELL_TYPE_STRING);
                        programme = programmeCell.getStringCellValue();
                        */ 
                        Cell genderCell = cellsIter.next();
                        genderCell.setCellType(Cell.CELL_TYPE_STRING);
                        gender = genderCell.getStringCellValue();


                        numberOfRecords++;

                        log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department);

                        EligibleBallotStudent eliStudent = this.getEligibleBallotStudentByStudentNumber(studentNumber);
                        if(eliStudent == null) {
                            Department departmentObj = this.getDepartmentById(department);
                            if(departmentObj == null) {
                                log.error("Department =" + departmentObj.getName() + " not found");
                            } else {
                                EligibleBallotStudent eligibleBallotStudent = this.addEligibleBallotStudent(studentNumber, studentName, departmentObj, gender);
                                uploadedEligibleBallotStudents.add(eligibleBallotStudent);
                            }    
                        } else {
                            log.warn("Eligible student already exists for " + studentNumber);
                        }

                    }

                }

            } else {
                HSSFWorkbook hssfWorkBook = new HSSFWorkbook(uploadedFile.getInputstream());
                HSSFSheet hssfWorkSheet = hssfWorkBook.getSheetAt(0);
                Iterator<Row> rowsIter = hssfWorkSheet.rowIterator();
                while (rowsIter.hasNext()) {
                    Row currentRow = rowsIter.next();
                    if(currentRow.getRowNum() == 0) {
                        continue;
                    }

                    Iterator<Cell> cellsIter = currentRow.cellIterator();
                    while (cellsIter.hasNext()) {
                        Cell studentNumberCell = cellsIter.next();
                        studentNumberCell.setCellType(Cell.CELL_TYPE_STRING);
                        studentNumber = studentNumberCell.getStringCellValue();

                        Cell studentNameCell = cellsIter.next();
                        studentNameCell.setCellType(Cell.CELL_TYPE_STRING);
                        studentName = studentNameCell.getStringCellValue();

                        Cell departmentCell = cellsIter.next();
                        departmentCell.setCellType(Cell.CELL_TYPE_STRING);
                        department = departmentCell.getStringCellValue();                            

                        Cell genderCell = cellsIter.next();
                        genderCell.setCellType(Cell.CELL_TYPE_STRING);
                        gender = genderCell.getStringCellValue();

                        numberOfRecords++;

                        log.info("studentNumber = " + studentNumber + ", studentName = " + studentName + " department = " + department +
                                ", gender = " + gender);

                        EligibleBallotStudent eliStudent = this.getEligibleBallotStudentByStudentNumber(studentNumber);
                        if(eliStudent == null) {
                            Department departmentObj = this.getDepartmentById(department);
                            if(departmentObj == null) {
                                log.error("Department =" + departmentObj.getName() + " not found");
                            } else {
                                EligibleBallotStudent eligibleBallotStudent = this.addEligibleBallotStudent(studentNumber, studentName, departmentObj, gender);
                                uploadedEligibleBallotStudents.add(eligibleBallotStudent);
                            }  
                        } else {
                            log.warn("Eligible student already exists for " + studentNumber);
                        }
                    }
                }
            }                             
            transaction.commit();
           
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
    
    private EligibleBallotStudent addEligibleBallotStudent(String studentNumber, String studentName, Department department, String gender) {
        EligibleBallotStudent eligibleBallotStudent = new EligibleBallotStudent();
        try {
            eligibleBallotStudent.setStudentNumber(studentNumber);
            eligibleBallotStudent.setFirstName(studentName);
            eligibleBallotStudent.setGender(gender);
            eligibleBallotStudent.setDepartment(department);
            //eligibleBallotStudent.setProgrammeOfStudy(programme);
                                    
            eligibleBallotStudent.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession().getSessionName());
            eligibleBallotStudent.setDateUploaded(Calendar.getInstance().getTime());
            eligibleBallotStudent.setUploadedBy(loginUserBean.getCurrentUser().getUserName());
            
            entityManager.persist(eligibleBallotStudent);
            log.info("Eligible student created for " + studentNumber);
            
                        
        } catch(Exception e) {
            log.error("Error addEligibleBallotStudent: " + e.getMessage());
        }
        
        return eligibleBallotStudent;
    }
    
    private Department getDepartmentById(String id) {
        Department department = null;
        try {
            department = entityManager.find(Department.class, Long.parseLong(id));
                      
            
        } catch(Exception e) {
            log.error("Error in getDepartmentById: " + e.getLocalizedMessage());
        }
        
        return department;
        
    }
    
    private HostelStudentType getHostelStudentType(String studentType) {
        HostelStudentType hostelStudentType = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelStudentTypeByStudentType");
            query.setParameter(1, studentType);
            
            hostelStudentType = (HostelStudentType)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelStudentType: " + e.getLocalizedMessage());
        }
        
        return hostelStudentType;
    }
    
    private EligibleBallotStudent getEligibleBallotStudentByStudentNumber(String studentNumber) {
        EligibleBallotStudent eligibleBallotStudent = null;
        try {
            Query query = entityManager.createNamedQuery("getEligibleBallotStudentByStudentNumber");
            query.setParameter(1, studentNumber);
            
            eligibleBallotStudent = (EligibleBallotStudent)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getEligibleBallotStudentByStudentNumber: " + e.getLocalizedMessage());
        }
        
        return eligibleBallotStudent;
    }
    
    private HostelRoomBedSpace getHostelRoomBedSpaceByPosition(String space) {
        HostelRoomBedSpace hostelRoomBedSpace = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelRoomBedSpaceByPosition");
            query.setParameter(1, space);
            
            hostelRoomBedSpace = (HostelRoomBedSpace)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelRoomBedSpaceByPosition: " + e.getLocalizedMessage());
        }
        
        return hostelRoomBedSpace;
    }
    
    private Hostel getHostelByName(String hostelName) {
        Hostel hostel = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelByName");
            query.setParameter(1, hostelName);
            
            hostel = (Hostel)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelByName: " + e.getLocalizedMessage());
        }
        
        return hostel;
    }
    
    private HostelRoom getHostelRoomByHostelAndRoomNumber(Hostel hostel, String roomNumber) {
        HostelRoom hostelRoom = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelRoomByHostelAndRoomNumber");
            query.setParameter(1, hostel.getId());
            query.setParameter(2, roomNumber);
            
            hostelRoom = (HostelRoom)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelRoomByHostelAndRoomNumber: " + e.getLocalizedMessage());
        }
        
        return hostelRoom;
    }
    
    public void search() {
       uploadedEligibleBallotStudents = new ArrayList<EligibleBallotStudent>();
       page = 0;
       queryEligibleBallotStudent();
    }

    public void nextPage() {
       page++;
       queryEligibleBallotStudent();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        queryEligibleBallotStudent();
     }
       
    private void queryEligibleBallotStudent() {
        String queryString = "";
        Query query = null;
        if (!queryString.equalsIgnoreCase("%%")) {
            queryString = "select t from EligibleBallotStudent t where lower(t.firstName) like '%" + searchParam + "%'"
                    + " or lower(t.department.name) like '%" + searchParam + "%' or lower(t.studentNumber) like '" + searchParam + "'";
            query = entityManager.createQuery(queryString);
            
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must enter a search parameter.",""));            
        }

        //eligibleBallotStudents = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        eligibleBallotStudents = query.getResultList();
    }

    public boolean isNextPageAvailable() {
        return eligibleBallotStudents != null && eligibleBallotStudents.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }
   
    public String getSearchPattern() {
        return searchParam == null
                ? "%" : '%' + searchParam.toLowerCase().replace('*', '%') + '%';
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

    public HostelSettingsHelper getHostelSettingsHelper() {
        return hostelSettingsHelper;
    }

    public void setHostelSettingsHelper(HostelSettingsHelper hostelSettingsHelper) {
        this.hostelSettingsHelper = hostelSettingsHelper;
    }

    public List<EligibleBallotStudent> getEligibleBallotStudents() {
        return eligibleBallotStudents;
    }

    public void setEligibleBallotStudents(List<EligibleBallotStudent> eligibleBallotStudents) {
        this.eligibleBallotStudents = eligibleBallotStudents;
    }

    public List<EligibleBallotStudent> getUploadedEligibleBallotStudents() {
        return uploadedEligibleBallotStudents;
    }

    public void setUploadedEligibleBallotStudents(List<EligibleBallotStudent> uploadedEligibleBallotStudents) {
        this.uploadedEligibleBallotStudents = uploadedEligibleBallotStudents;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public int getUploadRowIndex() {
        return ++uploadRowIndex;
    }

    public void setUploadRowIndex(int uploadRowIndex) {
        this.uploadRowIndex = uploadRowIndex;
    }
    
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
}
