package com.simbest.boot.sys.service.impl;

import com.google.common.collect.Lists;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.sys.model.UploadFileResponse;
import com.simbest.boot.sys.repository.SysFileRepository;
import com.simbest.boot.sys.service.ISysFileService;
import com.simbest.boot.util.AppFileUtil;
import com.simbest.boot.util.SpringContextUtil;
import com.simbest.boot.util.office.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用途：统一系统文件管理逻辑层
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
@Slf4j
@Service
@DependsOn(value = {"appFileUtil"})
public class SysFileService extends LogicService<SysFile, String> implements ISysFileService {

    @Autowired
    private SysFileRepository repository;

    @Autowired
    private AppFileUtil appFileUtil;

    @Autowired
    private AppConfig config;

    @Autowired
    private SpringContextUtil springContextUtil;

    public StoreLocation serverUploadLocation;

    @Autowired
    public SysFileService(SysFileRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        serverUploadLocation = Enum.valueOf(StoreLocation.class, config.getUploadLocation());
    }

    @Override
    public SysFile uploadProcessFile(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> fileList = uploadProcessFiles(Arrays.asList(multipartFile), pmInsType, pmInsId, pmInsTypePart);
        return fileList.isEmpty() ? null : fileList.get(0);
    }

    @Override
    public SysFile uploadProcessFile ( MultipartFile multipartFile,String customDirectory,String pmInsType, String pmInsId, String pmInsTypePart ) {
        List<SysFile> fileList = uploadProcessFiles(Arrays.asList(multipartFile),customDirectory,pmInsType, pmInsId, pmInsTypePart);
        return fileList.isEmpty() ? null : fileList.get(0);
    }

    @Override
    @Transactional
    public List<SysFile> uploadProcessFiles(Collection<MultipartFile> multipartFiles, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> sysFileList = Lists.newArrayList();
        try {
            sysFileList = appFileUtil.uploadFiles(pmInsType + ApplicationConstants.SLASH + pmInsTypePart, multipartFiles);
            for(SysFile sysFile : sysFileList){
                sysFile = super.insert(sysFile); //先保存文件获取ID
                sysFile.setDownLoadUrl(sysFile.getDownLoadUrl().concat("?id="+sysFile.getId())); //修改下载URL，追加ID
                sysFile.setApiFilePath(sysFile.getApiFilePath().concat("?id="+sysFile.getId()));
                sysFile.setAnonymousFilePath(sysFile.getAnonymousFilePath().concat("?id="+sysFile.getId()));
                sysFile.setPmInsType(pmInsType);
                sysFile.setPmInsId(pmInsId);
                sysFile.setPmInsTypePart(pmInsTypePart);
                String mobileFilePath = null;
                String apiFilePath = null;
                String anonymousFilePath = null;
                switch (serverUploadLocation) {
                    case disk:
                        mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getDownLoadUrl();
                        apiFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getApiFilePath();
                        anonymousFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getAnonymousFilePath();
                        break;
                    case fastdfs:
                        mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + sysFile.getFilePath();
                        apiFilePath = mobileFilePath;
                        anonymousFilePath = mobileFilePath;
                        break;
                    case ftp:
                    case sftp:
                        mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getDownLoadUrl();
                        apiFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getApiFilePath();
                        anonymousFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getAnonymousFilePath();
                        break;
                }
                sysFile.setMobileFilePath( mobileFilePath );
                sysFile.setApiFilePath(apiFilePath);
                sysFile.setAnonymousFilePath(anonymousFilePath);
                super.update(sysFile); //再保存一下更新的值
            }
        } catch (IOException e) {
            Exceptions.printException(e);
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return sysFileList;
    }

    @Override
    public List<SysFile> uploadProcessFiles ( Collection<MultipartFile> multipartFiles,String customDirectory,String pmInsType, String pmInsId, String pmInsTypePart ) {
        List<SysFile> sysFileList = Lists.newArrayList();
        try {
            sysFileList = appFileUtil.customUploadFiles(customDirectory + pmInsType + ApplicationConstants.SLASH + pmInsTypePart, multipartFiles);
            for(SysFile sysFile : sysFileList){
                sysFile = super.insert(sysFile); //先保存文件获取ID
                sysFile.setDownLoadUrl(sysFile.getDownLoadUrl().concat("?id="+sysFile.getId())); //修改下载URL，追加ID
                sysFile.setApiFilePath(sysFile.getApiFilePath().concat("?id="+sysFile.getId()));
                sysFile.setAnonymousFilePath(sysFile.getAnonymousFilePath().concat("?id="+sysFile.getId()));
                sysFile.setPmInsType(pmInsType);
                sysFile.setPmInsId(pmInsId);
                sysFile.setPmInsTypePart(pmInsTypePart);
                String mobileFilePath = null;
                String apiFilePath = null;
                String anonymousFilePath = null;
                switch (serverUploadLocation) {
                    case disk:
                        mobileFilePath = config.getShareHostPost() + ApplicationConstants.SLASH + sysFile.getFileName();
                        apiFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getApiFilePath();
                        anonymousFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getAnonymousFilePath();
                        break;
                    case fastdfs:
                        mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + sysFile.getFilePath();
                        apiFilePath = mobileFilePath;
                        anonymousFilePath = mobileFilePath;
                        break;
                    case ftp:
                    case sftp:
                        mobileFilePath = config.getShareHostPost() + ApplicationConstants.SLASH + sysFile.getFileName();
                        apiFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getApiFilePath();
                        anonymousFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getAnonymousFilePath();
                        break;
                }
                sysFile.setMobileFilePath( mobileFilePath );
                sysFile.setApiFilePath(apiFilePath);
                sysFile.setAnonymousFilePath(anonymousFilePath);
                super.update(sysFile); //再保存一下更新的值
            }
        } catch (IOException e) {
            Exceptions.printException(e);
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return sysFileList;
    }

    @Override
    public <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                List<T> listData = importUtil.importExcel(sheetName, new FileInputStream(tempFile));
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setListData(listData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
            }
        }
        return null;
    }

    @Override
    public <T> UploadFileResponse importExcel ( MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName, int inputRow ) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                List<T> listData = importUtil.importExcel(sheetName, new FileInputStream(tempFile),inputRow);
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setListData(listData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
            }
        }
        return null;
    }

    @Override
    public <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                Map<String, List<T>> mapData = importUtil.importExcel(new FileInputStream(tempFile));
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setMapData(mapData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
            }
        }
        return null;
    }

    @Override
    public File getRealFileById(String id) {
        SysFile sysFile = this.findById(id);
        return appFileUtil.getFileFromSystem(sysFile);
    }

    @Override
    @Transactional
    public void deleteById ( String id ) {
        SysFile sysFile = this.findById(id);
        String filePath = sysFile.getFilePath();
        super.deleteById(id);
        boolean result = appFileUtil.deleteFile(sysFile);
        log.warn("物理删除文件结果为【{}】", result);
    }
}
