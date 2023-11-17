/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.amazonaws.auth.AWSCredentials
 *  com.amazonaws.auth.AWSCredentialsProvider
 *  com.amazonaws.auth.AWSStaticCredentialsProvider
 *  com.amazonaws.auth.BasicAWSCredentials
 *  com.amazonaws.auth.profile.ProfileCredentialsProvider
 *  com.amazonaws.services.glacier.AmazonGlacier
 *  com.amazonaws.services.glacier.AmazonGlacierClientBuilder
 *  com.amazonaws.services.glacier.TreeHashGenerator
 *  com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest
 *  com.amazonaws.services.glacier.model.CompleteMultipartUploadResult
 *  com.amazonaws.services.glacier.model.DeleteArchiveRequest
 *  com.amazonaws.services.glacier.model.DescribeVaultOutput
 *  com.amazonaws.services.glacier.model.GetJobOutputRequest
 *  com.amazonaws.services.glacier.model.GetJobOutputResult
 *  com.amazonaws.services.glacier.model.GlacierJobDescription
 *  com.amazonaws.services.glacier.model.InitiateJobRequest
 *  com.amazonaws.services.glacier.model.InitiateJobResult
 *  com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest
 *  com.amazonaws.services.glacier.model.InitiateMultipartUploadResult
 *  com.amazonaws.services.glacier.model.JobParameters
 *  com.amazonaws.services.glacier.model.ListJobsRequest
 *  com.amazonaws.services.glacier.model.ListJobsResult
 *  com.amazonaws.services.glacier.model.ListVaultsRequest
 *  com.amazonaws.services.glacier.model.ListVaultsResult
 *  com.amazonaws.services.glacier.model.UploadMultipartPartRequest
 *  com.amazonaws.services.glacier.model.UploadMultipartPartResult
 *  com.amazonaws.util.BinaryUtils
 */
package com.crushftp.client;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.AmazonGlacierClientBuilder;
import com.amazonaws.services.glacier.TreeHashGenerator;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.GlacierJobDescription;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ListJobsRequest;
import com.amazonaws.services.glacier.model.ListJobsResult;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;
import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.URLConnection;
import com.crushftp.client.VRL;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

public class GlacierClient
extends GenericClient {
    private String region_host = "glacier.us-east-1.amazonaws.com";
    private String region = "us-east-1";
    private String glacier_root = "./glacier/";
    private String vaultName0 = null;
    private Vector replicating = null;
    private String partSize = "1048576";
    private SimpleDateFormat yyyyMMddtHHmmssSSS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S", Locale.US);
    private SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    private AmazonGlacier sdk_client;
    private AWSStaticCredentialsProvider credential_provider;
    private int folder_delete_depth = 3;
    private Vector delete_xml = new Vector();

    public GlacierClient(String url, String header, Vector log) {
        super(header, log);
        this.fields = new String[]{"username", "password", "delete_xml_representation_files", "partSize"};
        if (!url.endsWith("/")) {
            url = String.valueOf(url) + "/";
        }
        this.url = url;
        VRL glacier_vrl = new VRL(url);
        this.region_host = glacier_vrl.getHost().toLowerCase();
        this.glacier_root = System.getProperty("crushftp.glacier_root", "./glacier/");
        this.replicating = (Vector)System.getProperties().get("crushftp.glacier_replicated");
        this.folder_delete_depth = Integer.parseInt(System.getProperty("crushftp.glacier_folder_delete_depth", "3"));
    }

    public String getRawXmlPath(String path) throws Exception {
        path = this.getPath(path);
        return String.valueOf(this.glacier_root) + this.vaultName0 + path;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private String getPath(String path) throws Exception {
        if (this.config.getProperty("delete_xml_representation_files", "false").equals("true")) {
            Vector vector = this.delete_xml;
            synchronized (vector) {
                Vector<Properties> temp = new Vector<Properties>();
                int x = 0;
                while (x < this.delete_xml.size()) {
                    Properties p = (Properties)this.delete_xml.get(x);
                    if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) < 1000L) {
                        Common.recurseDelete(String.valueOf(this.glacier_root) + this.vaultName0 + p.getProperty("path"), false);
                        temp.add(p);
                    }
                    ++x;
                }
                if (temp.size() > 0) {
                    this.delete_xml.removeAll(temp);
                }
            }
        }
        if (this.vaultName0 == null) {
            String vault_name = path.substring(1, path.indexOf("/", 1));
            Properties vault = this.stat("/" + vault_name);
            if (vault == null) {
                throw new Exception("Could not found vault! Path : " + path);
            }
            this.vaultName0 = vault_name;
            new File_S(String.valueOf(this.glacier_root) + this.vaultName0).mkdirs();
        }
        return Common.dots(path.substring(path.indexOf("/", 1)));
    }

    public static void writeFs(String glacier_root, String vaultName0, Vector replicating, String path0, Properties p) throws Exception {
        String path = Common.dots(path0);
        new File_S(String.valueOf(glacier_root) + vaultName0 + Common.all_but_last(path)).mkdirs();
        Common.writeXMLObject(String.valueOf(glacier_root) + vaultName0 + path, p, "glacier");
        if (replicating != null) {
            Properties p2 = new Properties();
            p2.put("vaultName0", vaultName0);
            p2.put("path", path0);
            p2.put("data", Common.CLONE(p));
            replicating.addElement(p2);
        }
    }

    @Override
    public String login2(String username, String password, String clientid) throws Exception {
        this.config.put("username", username.trim());
        this.config.put("password", password.trim());
        try {
            AmazonGlacierClientBuilder builder = AmazonGlacierClientBuilder.standard();
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(this.config.getProperty("username"), this.config.getProperty("password"));
            AWSStaticCredentialsProvider credential_provider = new AWSStaticCredentialsProvider((AWSCredentials)awsCreds);
            ProfileCredentialsProvider credentials = new ProfileCredentialsProvider();
            this.region = this.region_host.substring(8).substring(0, this.region_host.substring(8).indexOf("."));
            this.sdk_client = (AmazonGlacier)((AmazonGlacierClientBuilder)((AmazonGlacierClientBuilder)builder.withCredentials((AWSCredentialsProvider)credential_provider)).withRegion(this.region)).build();
        }
        catch (Exception e) {
            this.log(e);
            throw e;
        }
        this.config.put("logged_out", "false");
        return "Success";
    }

    @Override
    public Vector list(String path, Vector list) throws Exception {
        this.vaultName0 = null;
        if (path.equals("/")) {
            try {
                ListVaultsRequest lvr = new ListVaultsRequest();
                lvr.withRequestCredentialsProvider((AWSCredentialsProvider)this.credential_provider);
                lvr.setLimit("10");
                ListVaultsResult result = this.sdk_client.listVaults(lvr);
                this.listVaults(list, result);
                while (result.getMarker() != null) {
                    if (this.config.getProperty("logged_out", "false").equals("true")) {
                        throw new Exception("Error: Cancel dir listing. The client is already closed.");
                    }
                    lvr.setMarker(result.getMarker());
                    result = this.sdk_client.listVaults(lvr);
                    this.listVaults(list, result);
                }
            }
            catch (Exception e) {
                this.log(e);
                throw e;
            }
        } else {
            path = this.getPath(path);
            if (!new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + path).exists()) {
                throw new Exception("No such folder: \"" + this.glacier_root + this.vaultName0 + path + "\"");
            }
            File_S[] f = (File_S[])new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + path).listFiles();
            int x = 0;
            while (f != null && x < f.length) {
                if (this.config.getProperty("logged_out", "false").equals("true")) {
                    throw new Exception("Error: Cancel dir listing. The client is already closed.");
                }
                if (!f[x].getName().equals(".DS_Store")) {
                    Date d = new Date(f[x].lastModified());
                    Properties p = null;
                    p = f[x].isFile() ? (Properties)Common.readXMLObject(f[x]) : new Properties();
                    String line = String.valueOf(f[x].isDirectory() ? "d" : "-") + "rwxrwxrwx   1    owner   group   " + p.getProperty("size", "0") + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + f[x].getName();
                    Properties stat = GlacierClient.parseStat(line);
                    stat.put("url", String.valueOf(this.url) + this.vaultName0 + path + stat.getProperty("name") + (f[x].isDirectory() ? "/" : ""));
                    if (p.containsKey("archiveId")) {
                        stat.put("archiveId", p.getProperty("archiveId"));
                    }
                    list.addElement(stat);
                }
                ++x;
            }
        }
        return list;
    }

    private void listVaults(Vector list, ListVaultsResult result) throws Exception {
        List vaults = result.getVaultList();
        int x = 0;
        while (x < vaults.size()) {
            DescribeVaultOutput dVO = (DescribeVaultOutput)vaults.get(x);
            Date d = this.yyyyMMddtHHmmssSSS.parse(dVO.getCreationDate());
            String line = "drwxrwxrwx   1    owner   group   " + dVO.getSizeInBytes() + "   " + this.yyyyMMddHHmmss.format(d) + "   " + this.dd.format(d) + " " + this.yyyy.format(d) + " /" + dVO.getVaultName();
            Properties stat = GlacierClient.parseStat(line);
            System.out.println(line);
            stat.put("url", String.valueOf(this.url) + stat.getProperty("name"));
            stat.put("arn", dVO.getVaultARN());
            stat.put("numberOfArchives", dVO.getNumberOfArchives());
            list.add(stat);
            ++x;
        }
    }

    @Override
    public Properties stat(String path) throws Exception {
        if (path.endsWith(":filetree")) {
            path = path.substring(0, path.indexOf(":filetree") - 1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        Vector v = new Vector();
        this.list(Common.all_but_last(path), v);
        int x = 0;
        while (x < v.size()) {
            Properties p = (Properties)v.elementAt(x);
            if (p.getProperty("name").equals(Common.last(path))) {
                return p;
            }
            ++x;
        }
        return null;
    }

    @Override
    protected InputStream download3(String path, long startPos, long endPos, boolean binary) throws Exception {
        throw new Exception("Download not supported!");
    }

    @Override
    protected OutputStream upload3(String path, long startPos, boolean truncate, boolean binary) throws Exception {
        String path_0 = this.getPath(path);
        if (this.vaultName0.equals("")) {
            throw new Exception("Missing Vault!");
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        String archive_id = "<m><v>4</v><p>" + Base64.encodeBytes(path_0.substring(1).getBytes("UTF8")) + "</p><lm>" + df.format(new Date()) + "</lm></m>";
        InitiateMultipartUploadRequest request = (InitiateMultipartUploadRequest)new InitiateMultipartUploadRequest().withVaultName(this.vaultName0).withArchiveDescription(archive_id).withPartSize(this.config.getProperty("partSize", this.partSize)).withRequestCredentialsProvider((AWSCredentialsProvider)this.credential_provider);
        InitiateMultipartUploadResult result = this.sdk_client.initiateMultipartUpload(request);
        int part_size = Integer.parseInt(this.config.getProperty("partSize", this.partSize));
        String uploadId = result.getUploadId();
        class OutputWrapper
        extends OutputStream {
            boolean closed = false;
            ByteArrayOutputStream baos;
            Vector binaryChecksums;
            long pos;
            long pos2;
            Properties p_upload;
            private final /* synthetic */ int val$part_size;
            private final /* synthetic */ String val$uploadId;
            private final /* synthetic */ String val$path_0;

            OutputWrapper(int n, String string, String string2) {
                this.val$part_size = n;
                this.val$uploadId = string;
                this.val$path_0 = string2;
                this.baos = new ByteArrayOutputStream(n);
                this.binaryChecksums = new Vector();
                this.pos = 0L;
                this.pos2 = 0L;
                this.p_upload = new Properties();
            }

            @Override
            public void write(int i) throws IOException {
                this.write(new byte[]{(byte)i}, 0, 1);
            }

            @Override
            public void write(byte[] b) throws IOException {
                this.write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (this.baos.size() + len > this.val$part_size) {
                    int chunks = (this.baos.size() + len) / this.val$part_size;
                    int diff = this.baos.size() + len - chunks * this.val$part_size;
                    int offset_len = len - diff;
                    this.baos.write(b, off, offset_len);
                    this.pos2 += (long)this.baos.size();
                    long tmp_pos = this.baos.size();
                    this.flushNow();
                    this.pos += tmp_pos;
                    this.baos.write(b, offset_len, len - offset_len);
                } else {
                    this.baos.write(b, off, len);
                }
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void close() throws IOException {
                block9: {
                    if (this.closed) {
                        return;
                    }
                    if (this.baos.size() > 0) {
                        this.pos2 += (long)this.baos.size();
                        this.flushNow();
                    }
                    String fianl_checksum = "";
                    try {
                        fianl_checksum = GlacierClient.this.calculateTreeHash(this.binaryChecksums);
                        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest().withVaultName(GlacierClient.this.vaultName0).withUploadId(this.val$uploadId).withChecksum(fianl_checksum).withArchiveSize(String.valueOf(this.pos2));
                        CompleteMultipartUploadResult compResult = GlacierClient.this.sdk_client.completeMultipartUpload(compRequest);
                        try {
                            this.p_upload.put("archiveId", compResult.getArchiveId());
                            this.p_upload.put("size", String.valueOf(this.pos2));
                            GlacierClient.writeFs(GlacierClient.this.glacier_root, GlacierClient.this.vaultName0, GlacierClient.this.replicating, this.val$path_0, this.p_upload);
                            GlacierClient.this.log("Upload finished. Archive id : " + compResult.getArchiveId());
                            if (!GlacierClient.this.config.getProperty("delete_xml_representation_files", "false").equals("true")) break block9;
                            Vector vector = GlacierClient.this.delete_xml;
                            synchronized (vector) {
                                Properties p = new Properties();
                                p.put("path", this.val$path_0);
                                p.put("time", String.valueOf(System.currentTimeMillis()));
                                GlacierClient.this.delete_xml.add(p);
                            }
                        }
                        catch (Exception e) {
                            GlacierClient.this.log("Add Tag : " + e);
                        }
                    }
                    catch (Exception e) {
                        GlacierClient.this.log(e);
                        throw new IOException(e);
                    }
                }
                this.closed = true;
            }

            public String flushNow() throws IOException {
                String result = "";
                if (this.baos.size() > 0) {
                    try {
                        String checksum = TreeHashGenerator.calculateTreeHash((InputStream)new ByteArrayInputStream(this.baos.toByteArray()));
                        String contentRange = "bytes " + this.pos + "-" + (this.pos2 - 1L) + "/*";
                        byte[] binaryChecksum = BinaryUtils.fromHex((String)checksum);
                        this.binaryChecksums.add(binaryChecksum);
                        UploadMultipartPartRequest partRequest = new UploadMultipartPartRequest().withVaultName(GlacierClient.this.vaultName0).withBody((InputStream)new ByteArrayInputStream(this.baos.toByteArray())).withChecksum(checksum).withRange(contentRange).withUploadId(this.val$uploadId);
                        UploadMultipartPartResult uploadMultipartPartResult = GlacierClient.this.sdk_client.uploadMultipartPart(partRequest);
                    }
                    catch (Exception e) {
                        GlacierClient.this.log("Upload part : " + this.pos + (this.pos2 - 1L) + " Error : " + e);
                    }
                }
                this.baos.reset();
                return result;
            }
        }
        this.out = new OutputWrapper(part_size, uploadId, path_0);
        return this.out;
    }

    @Override
    public boolean delete(String path) throws Exception {
        Properties p = this.stat(path);
        if (p == null) {
            new Exception("Item not found! Path : " + path);
        }
        if (this.vaultName0 == null) {
            throw new Exception("Vault cannot be deleted!");
        }
        if (p.getProperty("type").equalsIgnoreCase("FILE")) {
            try {
                DeleteArchiveRequest dar = new DeleteArchiveRequest().withVaultName(this.vaultName0).withArchiveId(p.getProperty("archiveId"));
                dar.withRequestCredentialsProvider((AWSCredentialsProvider)this.credential_provider);
                this.sdk_client.deleteArchive(dar);
                Common.recurseDelete(String.valueOf(this.glacier_root) + this.vaultName0 + this.getPath(path), false);
            }
            catch (Exception e) {
                this.log(e);
                throw e;
            }
        }
        String path0 = this.getPath(path);
        Vector items = new Vector();
        Common.appendListing_S(String.valueOf(this.glacier_root) + this.vaultName0 + path0, items, "", this.folder_delete_depth, false);
        int x = 0;
        while (x < items.size()) {
            File_S f = (File_S)items.elementAt(x);
            if (!f.getName().equals(".DS_Store") && !f.isDirectory()) {
                Properties file = (Properties)Common.readXMLObject(f);
                try {
                    DeleteArchiveRequest dar = new DeleteArchiveRequest().withVaultName(this.vaultName0).withArchiveId(file.getProperty("archiveId"));
                    dar.withRequestCredentialsProvider((AWSCredentialsProvider)this.credential_provider);
                    this.sdk_client.deleteArchive(dar);
                    Common.recurseDelete(f.getAbsolutePath(), false);
                }
                catch (Exception e) {
                    this.log(e);
                    throw e;
                }
            }
            ++x;
        }
        Common.recurseDelete(String.valueOf(this.glacier_root) + this.vaultName0 + this.getPath(path), false);
        return true;
    }

    @Override
    public boolean rename(String rnfr, String rnto, boolean overwrite) throws Exception {
        rnfr = this.getPath(rnfr);
        rnto = this.getPath(rnto);
        if (this.vaultName0 == null) {
            throw new Exception("Vault cannot be renamed!");
        }
        return new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + rnfr).renameTo(new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + rnto));
    }

    @Override
    public boolean makedir(String path0) throws Exception {
        if (!(path0 = this.getPath(path0)).endsWith("/")) {
            path0 = String.valueOf(path0) + "/";
        }
        if (path0.equals("/")) {
            return true;
        }
        return new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + path0).mkdirs();
    }

    @Override
    public boolean makedirs(String path0) throws Exception {
        return this.makedir(path0);
    }

    @Override
    public boolean mdtm(String path, long modified) throws Exception {
        String path0 = this.getPath(path);
        return new File_S(String.valueOf(this.glacier_root) + this.vaultName0 + path0).setLastModified(modified);
    }

    public String createGetInventoryJob() throws Exception {
        if (this.vaultName0 == null || this.vaultName0.equals("")) {
            throw new Exception("Vault must be specified!");
        }
        InitiateJobRequest initJobRequest = new InitiateJobRequest().withVaultName(this.vaultName0).withJobParameters(new JobParameters().withType("inventory-retrieval"));
        InitiateJobResult initJobResult = this.sdk_client.initiateJob(initJobRequest);
        return initJobResult.getJobId();
    }

    public String getJobStatus(String job_id) throws Exception {
        if (this.vaultName0 == null || this.vaultName0.equals("")) {
            throw new Exception("Vault must be specified!");
        }
        ListJobsRequest ljr = new ListJobsRequest();
        ljr.withVaultName(this.vaultName0);
        ListJobsResult listJobResult = this.sdk_client.listJobs(ljr);
        List jobs = listJobResult.getJobList();
        int x = 0;
        while (x < jobs.size()) {
            GlacierJobDescription gjd = (GlacierJobDescription)jobs.get(x);
            if (gjd.getJobId().equals(job_id)) {
                return gjd.getStatusCode();
            }
            ++x;
        }
        return "";
    }

    public String downloadInventory(String job_id) throws Exception {
        GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest().withVaultName(this.vaultName0).withJobId(job_id);
        GetJobOutputResult jobOutputResult = this.sdk_client.getJobOutput(jobOutputRequest);
        return URLConnection.consumeResponse(jobOutputResult.getBody());
    }

    public String getVaultName() {
        return this.vaultName0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void logout() throws Exception {
        this.config.put("logged_out", "true");
        if (this.config.getProperty("delete_xml_representation_files", "false").equals("true")) {
            Vector vector = this.delete_xml;
            synchronized (vector) {
                int x = 0;
                while (x < this.delete_xml.size()) {
                    Properties p = (Properties)this.delete_xml.get(x);
                    Common.recurseDelete(String.valueOf(this.glacier_root) + this.vaultName0 + p.getProperty("path"), false);
                    ++x;
                }
                this.delete_xml.clear();
            }
        }
    }

    private String calculateTreeHash(Vector checksums) throws Exception {
        Vector hashes = new Vector();
        hashes.addAll(checksums);
        while (hashes.size() > 1) {
            Vector<Object> treeHashes = new Vector<Object>();
            int i = 0;
            while (i < hashes.size() / 2) {
                byte[] firstPart = (byte[])hashes.get(2 * i);
                byte[] secondPart = (byte[])hashes.get(2 * i + 1);
                byte[] concatenation = new byte[firstPart.length + secondPart.length];
                System.arraycopy(firstPart, 0, concatenation, 0, firstPart.length);
                System.arraycopy(secondPart, 0, concatenation, firstPart.length, secondPart.length);
                try {
                    treeHashes.add(this.computeSHA256Hash(concatenation));
                }
                catch (Exception e) {
                    this.log(e);
                    throw new Exception("Unable to compute hash", e);
                }
                ++i;
            }
            if (hashes.size() % 2 == 1) {
                treeHashes.add(hashes.get(hashes.size() - 1));
            }
            hashes = treeHashes;
        }
        return BinaryUtils.toHex((byte[])((byte[])hashes.get(0)));
    }

    private byte[] computeSHA256Hash(byte[] data) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[16384];
            int bytesRead = -1;
            while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
            byte[] byArray = messageDigest.digest();
            return byArray;
        }
        finally {
            try {
                bis.close();
            }
            catch (Exception e) {
                this.log(e);
            }
        }
    }
}

