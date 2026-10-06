package com.srm.events.service;

import com.srm.events.dao.interfaces.*;
import com.srm.events.dto.DocumentTypeDTO;
import com.srm.events.model.*;
import com.srm.events.service.impl.DocumentServiceImpl;
import com.srm.events.service.interfaces.DocumentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceImplTest {
    private DocumentDAO documents; private ApplicationDAO applications; private StudentDAO students;
    private FacultyDAO faculty; private EventDAO events; private DocumentStorage storage;
    private StudentAchievementDAO achievementDocuments;
    private DocumentServiceImpl service; private Application application; private Student owner;
    private StudentAchievement achievement;
    @TempDir Path temp;

    @BeforeEach void setUp(){
        documents=mock(DocumentDAO.class);applications=mock(ApplicationDAO.class);students=mock(StudentDAO.class);
        faculty=mock(FacultyDAO.class);events=mock(EventDAO.class);storage=mock(DocumentStorage.class);achievementDocuments=mock(StudentAchievementDAO.class);
        service=new DocumentServiceImpl(documents,applications,students,faculty,events,storage,achievementDocuments,8);
        application=new Application();application.setApplicationId(1L);application.setCreatedByStudentId(10L);application.setCurrentStatus(ApplicationStatus.SUBMITTED);
        owner=new Student();owner.setStudentId(10L);
        achievement=new StudentAchievement();achievement.setAchievementId(11L);achievement.setStudentId(10L);achievement.setVerificationStatus("PENDING_MENTOR_VERIFICATION");
        when(applications.findById(1L)).thenReturn(Optional.of(application));
        when(students.findByUserId(100L)).thenReturn(Optional.of(owner));
        when(achievementDocuments.findById(11L)).thenReturn(Optional.of(achievement));
        when(documents.findActiveTypeById(2L)).thenReturn(Optional.of(new DocumentTypeDTO(2L,"Supporting Document","PRE_EVENT")));
    }

    @Test void rejectsEmptyFile(){assertStatus(HttpStatus.BAD_REQUEST,()->service.upload(1L,2L,new MockMultipartFile("file","a.pdf","application/pdf",new byte[0]),100L,"STUDENT"));}
    @Test void rejectsUnsupportedExtension(){assertStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE,()->service.upload(1L,2L,new MockMultipartFile("file","a.exe","application/octet-stream",new byte[]{1}),100L,"STUDENT"));}
    @Test void rejectsInvalidMime(){assertStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE,()->service.upload(1L,2L,new MockMultipartFile("file","a.pdf","image/png","%PDF-".getBytes()),100L,"STUDENT"));}
    @Test void rejectsOversizedFile(){assertStatus(HttpStatus.PAYLOAD_TOO_LARGE,()->service.upload(1L,2L,new MockMultipartFile("file","a.pdf","application/pdf","%PDF-more".getBytes()),100L,"STUDENT"));}
    @Test void rejectsTraversalFilename(){assertStatus(HttpStatus.BAD_REQUEST,()->service.upload(1L,2L,new MockMultipartFile("file","../a.pdf","application/pdf","%PDF-".getBytes()),100L,"STUDENT"));}
    @Test void rejectsUnrelatedStudent(){Student other=new Student();other.setStudentId(99L);when(students.findByUserId(200L)).thenReturn(Optional.of(other));assertStatus(HttpStatus.FORBIDDEN,()->service.list(1L,200L,"STUDENT"));}
    @Test void rejectsUnassignedMentor(){Faculty mentor=new Faculty();mentor.setFacultyId(7L);when(faculty.findActiveByUserId(300L)).thenReturn(Optional.of(mentor));when(applications.isMentorAssignedToApplication(1L,7L)).thenReturn(false);assertStatus(HttpStatus.FORBIDDEN,()->service.list(1L,300L,"MENTOR"));}
    @Test void rejectsUnrelatedHod(){Faculty hod=new Faculty();hod.setDepartmentId(8L);when(faculty.findActiveByUserId(400L)).thenReturn(Optional.of(hod));when(applications.isApplicationInDepartment(1L,8L)).thenReturn(false);assertStatus(HttpStatus.FORBIDDEN,()->service.list(1L,400L,"HOD"));}
    @Test void missingDocumentIsNotFound(){when(documents.findById(9L)).thenReturn(Optional.empty());assertStatus(HttpStatus.NOT_FOUND,()->service.download(9L,100L,"STUDENT"));}
    @Test void ownerCanDownloadExistingDocument() throws Exception {Path file=Files.writeString(temp.resolve("stored.pdf"),"%PDF-");DocumentMetadata metadata=new DocumentMetadata();metadata.setDocumentId(5L);metadata.setApplicationId(1L);metadata.setStorageKey("stored.pdf");when(documents.findById(5L)).thenReturn(Optional.of(metadata));when(storage.resolve("stored.pdf")).thenReturn(file);assertEquals(file,service.download(5L,100L,"STUDENT").path());}
    @Test void ownerCanViewOwnActivityPngJpegAndPdfThroughTheAuthorizedDownloadPath() throws Exception {String[] names={"stored.png","stored.jpg","stored.pdf"};String[] mimes={"image/png","image/jpeg","application/pdf"};byte[][] content={{(byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A},{(byte)0xFF,(byte)0xD8,(byte)0xFF},"%PDF-".getBytes()};for(int i=0;i<names.length;i++){Path file=Files.write(temp.resolve(names[i]),content[i]);DocumentMetadata metadata=new DocumentMetadata();metadata.setDocumentId(50L+i);metadata.setApplicationId(1L);metadata.setStorageKey(names[i]);metadata.setMimeType(mimes[i]);when(documents.findById(50L+i)).thenReturn(Optional.of(metadata));when(storage.resolve(names[i])).thenReturn(file);assertEquals(file,service.download(50L+i,100L,"STUDENT").path());}}
    @Test void assignedMentorCanDownloadActivityEvidence() throws Exception {DocumentMetadata metadata=new DocumentMetadata();metadata.setDocumentId(60L);metadata.setApplicationId(1L);metadata.setStorageKey("mentor.pdf");when(documents.findById(60L)).thenReturn(Optional.of(metadata));when(storage.resolve("mentor.pdf")).thenReturn(Files.writeString(temp.resolve("mentor.pdf"),"%PDF-"));Faculty mentor=new Faculty();mentor.setFacultyId(7L);when(faculty.findActiveByUserId(300L)).thenReturn(Optional.of(mentor));when(applications.isMentorAssignedToApplication(1L,7L)).thenReturn(true);assertDoesNotThrow(()->service.download(60L,300L,"MENTOR"));}
    @Test void departmentHodCanDownloadActivityEvidence() throws Exception {DocumentMetadata metadata=new DocumentMetadata();metadata.setDocumentId(62L);metadata.setApplicationId(1L);metadata.setStorageKey("hod-owned.pdf");when(documents.findById(62L)).thenReturn(Optional.of(metadata));when(storage.resolve("hod-owned.pdf")).thenReturn(Files.writeString(temp.resolve("hod-owned.pdf"),"%PDF-"));Faculty hod=new Faculty();hod.setDepartmentId(8L);when(faculty.findActiveByUserId(400L)).thenReturn(Optional.of(hod));when(applications.isApplicationInDepartment(1L,8L)).thenReturn(true);assertDoesNotThrow(()->service.download(62L,400L,"HOD"));}
    @Test void unrelatedHodCannotDownloadActivityEvidence(){DocumentMetadata metadata=new DocumentMetadata();metadata.setDocumentId(61L);metadata.setApplicationId(1L);metadata.setStorageKey("hod.pdf");when(documents.findById(61L)).thenReturn(Optional.of(metadata));Faculty hod=new Faculty();hod.setDepartmentId(8L);when(faculty.findActiveByUserId(400L)).thenReturn(Optional.of(hod));when(applications.isApplicationInDepartment(1L,8L)).thenReturn(false);assertStatus(HttpStatus.FORBIDDEN,()->service.download(61L,400L,"HOD"));}
    @Test void validPdfUploadStoresOpaqueKey(){MockMultipartFile file=new MockMultipartFile("file","proof.pdf","application/pdf","%PDF-".getBytes());when(storage.store(file,"pdf")).thenReturn("opaque.pdf");when(documents.create(any())).thenAnswer(invocation->{DocumentMetadata value=invocation.getArgument(0);value.setDocumentId(4L);value.setDocumentName("Supporting Document");value.setDocumentPhase("PRE_EVENT");value.setVerificationStatus("PENDING");value.setUploadedAt(java.time.LocalDateTime.now());return value;});assertEquals(4L,service.upload(1L,2L,file,100L,"STUDENT").getDocumentId());}
    @Test void studentCanUploadPngAchievementEvidence(){assertValidAchievementUpload("proof.png","image/png",new byte[]{(byte)0x89,0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A});}
    @Test void studentCanUploadJpegAchievementEvidence(){assertValidAchievementUpload("proof.jpg","image/jpeg",new byte[]{(byte)0xFF,(byte)0xD8,(byte)0xFF});}
    @Test void studentCanUploadPdfAchievementEvidence(){assertValidAchievementUpload("proof.pdf","application/pdf","%PDF-".getBytes());}
    @Test void rejectsOversizedAchievementEvidence(){assertStatus(HttpStatus.PAYLOAD_TOO_LARGE,()->service.uploadAchievement(11L,2L,new MockMultipartFile("file","proof.pdf","application/pdf","%PDF-more".getBytes()),100L,"STUDENT"));}
    @Test void rejectsUnsupportedAchievementEvidence(){assertStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE,()->service.uploadAchievement(11L,2L,new MockMultipartFile("file","proof.exe","application/octet-stream",new byte[]{1}),100L,"STUDENT"));}
    @Test void rejectsSpoofedAchievementEvidence(){assertStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE,()->service.uploadAchievement(11L,2L,new MockMultipartFile("file","proof.png","image/png","%PDF-".getBytes()),100L,"STUDENT"));}
    @Test void rejectsAnotherStudentAchievementUpload(){Student other=new Student();other.setStudentId(99L);when(students.findByUserId(200L)).thenReturn(Optional.of(other));assertStatus(HttpStatus.FORBIDDEN,()->service.uploadAchievement(11L,2L,new MockMultipartFile("file","proof.pdf","application/pdf","%PDF-".getBytes()),200L,"STUDENT"));}
    @Test void assignedMentorCanViewAchievementEvidence(){Faculty mentor=new Faculty();mentor.setFacultyId(7L);when(faculty.findActiveByUserId(300L)).thenReturn(Optional.of(mentor));when(achievementDocuments.isMentorAssigned(11L,7L)).thenReturn(true);assertDoesNotThrow(()->service.listAchievement(11L,300L,"MENTOR"));}
    @Test void unrelatedMentorCannotViewAchievementEvidence(){Faculty mentor=new Faculty();mentor.setFacultyId(7L);when(faculty.findActiveByUserId(300L)).thenReturn(Optional.of(mentor));when(achievementDocuments.isMentorAssigned(11L,7L)).thenReturn(false);assertStatus(HttpStatus.FORBIDDEN,()->service.listAchievement(11L,300L,"MENTOR"));}
    @Test void departmentHodCanViewAchievementEvidence(){Faculty hod=new Faculty();hod.setDepartmentId(8L);when(faculty.findActiveByUserId(400L)).thenReturn(Optional.of(hod));when(achievementDocuments.isInDepartment(11L,8L)).thenReturn(true);assertDoesNotThrow(()->service.listAchievement(11L,400L,"HOD"));}
    @Test void unrelatedHodCannotViewAchievementEvidence(){Faculty hod=new Faculty();hod.setDepartmentId(8L);when(faculty.findActiveByUserId(400L)).thenReturn(Optional.of(hod));when(achievementDocuments.isInDepartment(11L,8L)).thenReturn(false);assertStatus(HttpStatus.FORBIDDEN,()->service.listAchievement(11L,400L,"HOD"));}
    @Test void rejectsPostEventUploadOnEventEndDate(){application.setCurrentStatus(ApplicationStatus.APPROVED);application.setEventId(3L);Event event=new Event();event.setEndDate(LocalDate.now());when(events.findById(3L)).thenReturn(Optional.of(event));when(documents.findActiveTypeById(3L)).thenReturn(Optional.of(new DocumentTypeDTO(3L,"Participation Proof","POST_EVENT")));MockMultipartFile file=new MockMultipartFile("file","proof.pdf","application/pdf","%PDF-".getBytes());assertStatus(HttpStatus.CONFLICT,()->service.upload(1L,3L,file,100L,"STUDENT"));}
    private void assertValidAchievementUpload(String name,String mime,byte[] content){MockMultipartFile file=new MockMultipartFile("file",name,mime,content);when(storage.store(file,name.substring(name.lastIndexOf('.')+1))).thenReturn("opaque-"+name);when(documents.create(any())).thenAnswer(invocation->{DocumentMetadata value=invocation.getArgument(0);value.setDocumentId(12L);value.setDocumentName("Supporting Document");value.setDocumentPhase("PRE_EVENT");value.setVerificationStatus("PENDING");value.setUploadedAt(java.time.LocalDateTime.now());return value;});assertEquals(12L,service.uploadAchievement(11L,2L,file,100L,"STUDENT").getDocumentId());}
    private void assertStatus(HttpStatus status,org.junit.jupiter.api.function.Executable executable){ResponseStatusException exception=assertThrows(ResponseStatusException.class,executable);assertEquals(status,exception.getStatusCode());}
}
