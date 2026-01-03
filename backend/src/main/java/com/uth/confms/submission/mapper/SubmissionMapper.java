package com.uth.confms.submission.mapper;

import com.uth.confms.submission.dto.*;
import com.uth.confms.submission.entity.Author;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.entity.SubmissionFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    
    SubmissionResponse toResponse(Submission submission);
    
    List<SubmissionResponse> toResponseList(List<Submission> submissions);
    
    AuthorResponse toAuthorResponse(Author author);
    
    List<AuthorResponse> toAuthorResponseList(List<Author> authors);
    
    FileResponse toFileResponse(SubmissionFile file);
    
    List<FileResponse> toFileResponseList(List<SubmissionFile> files);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submission", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Author toAuthor(SubmissionRequest.AuthorRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conferenceId", ignore = true)
    @Mapping(target = "trackId", ignore = true)
    @Mapping(target = "submitterId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submissionNumber", ignore = true)
    @Mapping(target = "isWithdrawn", ignore = true)
    @Mapping(target = "withdrawnAt", ignore = true)
    @Mapping(target = "withdrawReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "files", ignore = true)
    void updateSubmissionFromRequest(UpdateSubmissionRequest request, @MappingTarget Submission submission);
}




