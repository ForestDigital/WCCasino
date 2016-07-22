package com.google.josiahparrish9844;

import com.sun.rowset.CachedRowSetImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.Part;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

@ManagedBean(name = "FileBean")
@SessionScoped
public class FileBean implements Serializable {

    private Part part;

    @ManagedProperty(value = "#{loginBean}")
    private loginBean lb;

    @Resource(name = "jdbc/wspFinal")
    private DataSource ds;

    public ResultSet getList() throws SQLException {
        Connection conn = ds.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(
                    "SELECT IMAGENAME, IMAGE FROM IMAGESTORAGE"
            );
            CachedRowSet crs = new CachedRowSetImpl();
            crs.populate(result);
            return crs;
        } finally {
            conn.close();
        }
    }

    public String downloadFile(String IMAGENAME) throws SQLException, IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext context = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) context.getResponse();
        ServletOutputStream outStream = response.getOutputStream();

        Connection conn = ds.getConnection();
        PreparedStatement selectQuery = conn.prepareStatement(
                "SELECT * FROM IMAGESTORAGE WHERE IMAGENAME=?");
        selectQuery.setString(1, IMAGENAME);

        ResultSet result = selectQuery.executeQuery();
        if (!result.next()) {
            facesContext.addMessage("downloadForm:download",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "file download failed for id = " + IMAGENAME, null));
        }

        String fileName = result.getString("IMAGENAME");
        Blob fileBlob = result.getBlob("IMAGE");

//        response.setContentType(fileType);
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");

        final int BYTES = 1024;
        int length = 0;
        InputStream in = fileBlob.getBinaryStream();
        byte[] bbuf = new byte[BYTES];

        while ((in != null) && ((length = in.read(bbuf)) != -1)) {
            outStream.write(bbuf, 0, length);
        }

        outStream.close();
        conn.close();

        return null;
    }

    public void uploadFile() throws IOException, SQLException {

        FacesContext facesContext = FacesContext.getCurrentInstance();

        Connection conn = ds.getConnection();

        InputStream inputStream;
        inputStream = null;
        try {
            inputStream = part.getInputStream();
            PreparedStatement insertQuery = conn.prepareStatement(
                    "INSERT INTO IMAGESTORAGE (IMAGENAME, IMAGE) "
                    + "VALUES (?,?)");
            insertQuery.setString(1, part.getSubmittedFileName());
            insertQuery.setBinaryStream(2, inputStream);

            int result = insertQuery.executeUpdate();
            if (result == 1) {
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                part.getSubmittedFileName()
                                + ": uploaded successfuly !!", null));
            } else {
                // if not 1, it must be an error.
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                result + " file uploaded", null));
            }
        } catch (IOException e) {
            facesContext.addMessage("uploadForm:upload",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "File upload failed !!", null));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void uploadProfile() throws IOException, SQLException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Connection conn = ds.getConnection();

        InputStream inputStream;
        inputStream = null;
        try {

            inputStream = part.getInputStream();
            PreparedStatement insertQuery = conn.prepareStatement(
                    "INSERT INTO IMAGESTORAGE (IMAGENAME, IMAGE) "
                    + "VALUES (?,?)");
            insertQuery.setString(1, part.getSubmittedFileName());
            insertQuery.setBinaryStream(2, inputStream);

            int result = insertQuery.executeUpdate();
            if (result == 1) {
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                part.getSubmittedFileName()
                                + ": uploaded successfuly !!", null));
                PreparedStatement ps1 = conn.prepareStatement(
                        "UPDATE USERS set IMAGENAME = ? "
                        + "WHERE USERNAME = ?"
                );

                ps1.setString(1, part.getSubmittedFileName());
                ps1.setString(2, part.getSubmittedFileName());
                ps1.executeUpdate();

                //FacesContext facesContext = FacesContext.getCurrentInstance();
//            loginBean lb
//            = (loginBean)facesContext.getApplication()
//      .createValueBinding("#{loginBean}").getValue(facesContext);
                lb.setImagename(part.getSubmittedFileName());
            } else {
                // if not 1, it must be an error.
                facesContext.addMessage("uploadForm:upload",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                result + " file uploaded", null));
            }
        } catch (IOException e) {
            facesContext.addMessage("uploadForm:upload",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "File upload failed !!", null));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Select a file to upload", null));
        }
        Part file = (Part) value;
        long size = file.getSize();
        if (size <= 0) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "the file is empty", null));
        }
        if (size > 1024 * 1024 * 10) { // 10 MB limit
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            size + "bytes: file too big (limit 10MB)", null));
        }
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

}
