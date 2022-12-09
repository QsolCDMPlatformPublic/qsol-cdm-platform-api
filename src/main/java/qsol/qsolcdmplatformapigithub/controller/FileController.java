package qsol.qsolcdmplatformapigithub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import qsol.qsolcdmplatformapigithub.config.data.LoginUser;
import qsol.qsolcdmplatformapigithub.config.data.UserSession;
import qsol.qsolcdmplatformapigithub.config.data.Validator;
import qsol.qsolcdmplatformapigithub.domain.CsvDetail;
import qsol.qsolcdmplatformapigithub.dto.response.CsvResponse;
import qsol.qsolcdmplatformapigithub.dto.request.CursorResult;
import qsol.qsolcdmplatformapigithub.exception.user.UnauthorizedException;
import qsol.qsolcdmplatformapigithub.service.FileService;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/csv-data")
@RestController
public class FileController {

    private static final int DEFAULT_SIZE = 700;
    private final FileService fileService;
//    private final Validator validator;

    @PostMapping()
    public ResponseEntity<CsvResponse> readFile(@LoginUser UserSession userSession, MultipartHttpServletRequest request) throws IOException {
//        validator.validateLogin(userSession);
        MultipartFile file = request.getFile("files");
        List<CsvDetail> csvDetails = fileService.read(file);
        assert file != null;
        return new ResponseEntity<>(new CsvResponse(file.getOriginalFilename(), csvDetails.size()), HttpStatus.OK);
    }



    @GetMapping()
    public CursorResult<CsvDetail> getCsvData(@LoginUser UserSession userSession, @RequestParam(required = false) Long cursorId, @RequestParam(required = false) Integer size) {
//        validator.validateLogin(userSession);

        if (size == null) {
            size = DEFAULT_SIZE;
        }
        return fileService.get(cursorId, PageRequest.of(0, size));
    }

    @DeleteMapping()
    public void deleteCsvData(@LoginUser UserSession userSession) {
//        validator.validateLogin(userSession);
        fileService.deleteAll();
    }
}
