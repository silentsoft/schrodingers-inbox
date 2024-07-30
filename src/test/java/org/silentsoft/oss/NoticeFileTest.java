package org.silentsoft.oss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoticeFileTest {

    @Test
    @DisabledIfSystemProperty(named = "skipRedundantTests", matches = "true")
    public void noticeFileTest() throws Exception {
        NoticeFileGenerator.NoticeFileBuilder noticeBuilder = NoticeFileGenerator.newInstance("Schrodinger's Inbox", "silentsoft.org");

        addFrontendLibraries(noticeBuilder);
        addBackendLibraries(noticeBuilder);

        String markdown = noticeBuilder.generate();

        StringBuilder stringBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(Paths.get(System.getProperty("user.dir"), "NOTICE.md").toFile());
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
        }
        Assertions.assertEquals(markdown.trim(), stringBuilder.toString().trim());
    }

    private void addFrontendLibraries(NoticeFileGenerator.NoticeFileBuilder noticeBuilder) throws Exception {
        File directory = Paths.get(System.getProperty("user.dir")).toFile();

        StringBuilder commandBuilder = new StringBuilder();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            commandBuilder.append("npx.cmd");
        } else {
            commandBuilder.append("npx");
        }
        commandBuilder.append(" license-checker --production --json --customPath " + Paths.get(getClass().getResource("format.json").toURI()).toAbsolutePath());

        Process process = Runtime.getRuntime().exec(commandBuilder.toString(), null, directory);
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        process.waitFor();

        ObjectMapper objectMapper = new ObjectMapper();
        LicenseSummaryJsonObject map = objectMapper.readValue(builder.toString(), LicenseSummaryJsonObject.class);
        map.remove(self());

        for (Map.Entry<String, LicenseDetailJsonObject> entry : map.entrySet()) {
            LicenseDetailJsonObject detail = entry.getValue();
            String lib = String.join(" ", detail.getName(), detail.getVersion());

            String url = detail.getUrl();
            url = (url != null) ? url : detail.getRepository();
            url = (url != null) ? url : "";
            if (url.contains("github.com") && url.endsWith(".git")) {
                url = url.substring(0, url.length() - ".git".length());
            }
            if ("".equals(url)) {
                if ("dotenv-expand 5.1.0".equals(lib)) {
                    url = "https://github.com/motdotla/dotenv-expand";
                } else if ("@nicolo-ribaudo/eslint-scope-5-internals 5.1.1-v1".equals(lib)) {
                    url = "https://github.com/nicolo-ribaudo/eslint-scope-5-internals";
                } else {
                    throw new RuntimeException(String.format("%s has an empty url.", lib));
                }
            }
            if ("esprima 1.2.2".equals(lib)) {
                detail.setLicenses("BSD-2-Clause");
            }
            if (detail.getLicenses().startsWith("(") && detail.getLicenses().endsWith(")")) {
                detail.setLicenses(detail.getLicenses().substring(1, detail.getLicenses().length() - 1));
                if (detail.getLicenses().contains(" OR ")) {
                    String[] licenses = detail.getLicenses().split(" OR ");
                    for (String license : licenses) {
                        noticeBuilder.addLibrary(detail.getName(), detail.getVersion(), url, retrieveLicenses(license));
                    }
                } else if (detail.getLicenses().contains(" AND ")) {
                    String[] licenses = detail.getLicenses().split(" AND ");
                    for (String license : licenses) {
                        noticeBuilder.addLibrary(detail.getName(), detail.getVersion(), url, retrieveLicenses(license));
                    }
                } else {
                    throw new RuntimeException(String.format("The given license '%s' cannot be recognized.", detail.getLicenses()));
                }
            } else {
                noticeBuilder.addLibrary(detail.getName(), detail.getVersion(), url, retrieveLicenses(detail.getLicenses()));
            }
        }
    }

    private License retrieveLicenses(String licenses) {
        if (License.of(licenses) != null) {
            return License.of(licenses);
        }

        if ("BlueOak-1.0.0".equals(licenses)) {
            return new License("BlueOak-1.0.0", getClass().getResourceAsStream("Blue-Oak-Model-License-1.0.0.md"));
        } else if ("CDDL 1.0".equals(licenses)) {
            return new License("CDDL 1.0", getClass().getResourceAsStream("CDDL-1.0.txt"));
        }

        throw new RuntimeException(String.format("No license file provided for %s.", licenses));
    }

    private String self() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(Paths.get(System.getProperty("user.dir"), "package.json").toFile());
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        PackageJsonObject packageJson = objectMapper.readValue(stringBuilder.toString(), PackageJsonObject.class);

        return String.join("@", packageJson.getName(), packageJson.getVersion());
    }

    private void addBackendLibraries(NoticeFileGenerator.NoticeFileBuilder noticeBuilder) throws Exception {
        File directory = Paths.get(System.getProperty("user.dir")).toFile();

        StringBuilder commandBuilder = new StringBuilder();
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            commandBuilder.append("mvnw.cmd");
        } else {
            commandBuilder.append("./mvnw");
        }
        commandBuilder.append(" license:add-third-party");

        Process process = Runtime.getRuntime().exec(commandBuilder.toString(), null, directory);
        process.waitFor();

        try (FileReader fileReader = new FileReader(new File(directory, "target/generated-sources/license/THIRD-PARTY.txt"));
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("Lists of ")) {
                    continue;
                }

                String[] artifactAndUrl = line.substring(line.lastIndexOf("(")+1, line.length()-1).split(" - ");
                String[] groupAndArtifactAndVersion = artifactAndUrl[0].split(":");
                String artifact = groupAndArtifactAndVersion[1];
                String version = groupAndArtifactAndVersion[2];
                String url = artifactAndUrl[1];

                String[] licenseNames = line.substring(1, line.substring(0, line.indexOf(artifactAndUrl[0])-3).lastIndexOf(")")).split("\\) \\(");
                if (licenseNames.length == 1) {
                    if ("CDDL/GPLv2+CE".equals(licenseNames[0])) {
                        licenseNames = new String[]{ "CDDL 1.1", "GPL2 w/ CPE" };
                    } else if ("MPL 2.0 or EPL 1.0".equals(licenseNames[0])) {
                        licenseNames = new String[]{ "MPL 2.0", "EPL 1.0" };
                    }
                }
                for (int i=0, j=licenseNames.length; i<j; i++) {
                    String license = licenseNames[i];
                    if ("antlr".equals(artifact) && "BSD License".equals(license)) {
                        licenseNames[i] = "BSD 3-Clause License";
                    } else if (artifact.startsWith("logback") && "GNU Lesser General Public License".equals(license)) {
                        licenseNames[i] = "GNU Lesser General Public License v2.1";
                    } else if ("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0) JavaBeans(TM".equals(license)) {
                        licenseNames[i] = "CDDL 1.0";
                    } else if ("javax.mail:mail:1.4.4".equals(artifactAndUrl[0]) && "CDDL".equals(license)) {
                        licenseNames[i] = "CDDL 1.0";
                    }
                }
                List<License> licenses = new ArrayList<>();
                for (String licenseName : licenseNames) {
                    License license = retrieveLicenses(licenseName);
                    licenses.add(license);
                }
                noticeBuilder.addLibrary(artifact, version, url, licenses.toArray(new License[0]));
            }
        }

        noticeBuilder.addLibrary("spring-boot-loader", "2.5.2", "https://spring.io/projects/spring-boot", License.of("Apache 2.0"));
        noticeBuilder.addLibrary("spring-boot-jarmode-layertools", "2.5.2", "https://spring.io/projects/spring-boot", License.of("Apache 2.0"));
    }

}
