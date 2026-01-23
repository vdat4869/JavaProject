# Báo Cáo Đánh Giá Module Review

**Ngày đánh giá:** $(date)  
**Module:** Review (Review, Scoring, và Discussion Workflows)  
**Mục đích:** Đánh giá xem module Review có hỗ trợ đầy đủ review form structure, scoring criteria, deadlines, discussion visibility, và double-blind anonymity hay không.

---

## 1. ⚠️ Review Form Structure và Scoring Criteria

### 1.1 Review Form Fields

**Trạng thái:** ⚠️ **Có nhưng không khớp giữa Frontend và Backend**

**Chi tiết:**
- ✅ **Backend Review Entity** có:
  - `summary` (TEXT) - Tóm tắt review
  - `strengths` (TEXT) - Điểm mạnh
  - `weaknesses` (TEXT) - Điểm yếu
  - `comments` (TEXT) - Comments chi tiết
  - `score` (ReviewScore enum) - Điểm đánh giá
  - `status` (ReviewStatus enum) - DRAFT hoặc SUBMITTED
  - `isConfidential` (Boolean) - Confidential flag
- ⚠️ **Frontend ReviewForm** có:
  - `overallRating` (1-5) - **KHÔNG CÓ trong backend**
  - `confidence` (1-5) - **KHÔNG CÓ trong backend**
  - `comments` - Có
  - `strengths` - Có
  - `weaknesses` - Có
  - `recommendation` (ACCEPT/REJECT/MINOR_REVISION/MAJOR_REVISION) - **KHÔNG CÓ trong backend**

**Files:**
- `backend/src/main/java/com/uth/confms/review/entity/Review.java`
- `backend/src/main/java/com/uth/confms/review/dto/ReviewSubmitDTO.java`
- `frontend/src/components/review/ReviewForm.tsx`

**Vấn đề:**
- ⚠️ Frontend form có `overallRating` và `confidence` nhưng backend không có fields này
- ⚠️ Frontend form có `recommendation` nhưng backend chỉ có `score` (ReviewScore enum)
- ⚠️ Có sự không khớp giữa frontend và backend, có thể gây lỗi khi submit review

**Khuyến nghị:**
1. Thêm `overallRating` (Integer, 1-5) vào Review entity hoặc xóa khỏi frontend
2. Thêm `confidence` (Integer, 1-5) vào Review entity hoặc xóa khỏi frontend
3. Map `recommendation` từ frontend sang `score` trong backend (hoặc thêm recommendation field)
4. Đảm bảo frontend và backend đồng bộ với nhau

---

### 1.2 Scoring Criteria

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **ReviewScore enum** có 7 levels:
  - `STRONG_ACCEPT` - Chấp nhận mạnh mẽ
  - `ACCEPT` - Chấp nhận
  - `WEAK_ACCEPT` - Chấp nhận yếu
  - `BORDERLINE` - Ranh giới (có thể chấp nhận hoặc từ chối)
  - `WEAK_REJECT` - Từ chối yếu
  - `REJECT` - Từ chối
  - `STRONG_REJECT` - Từ chối mạnh mẽ
- ✅ Score được validate trong ReviewSubmitDTO (`@NotBlank`)
- ✅ Score được convert từ String sang ReviewScore enum trong ReviewService

**Files:**
- `backend/src/main/java/com/uth/confms/review/entity/Review.java` (ReviewScore enum)

**Features:**
- ✅ Comprehensive scoring scale từ STRONG_ACCEPT đến STRONG_REJECT
- ✅ Proper enum validation
- ✅ Score được lưu trong database

**Có thể cải thiện:**
- ⚠️ Không có numeric score (ví dụ: 1-7 scale) để tính average score
- ⚠️ Không có mapping từ recommendation (ACCEPT/REJECT/MINOR_REVISION/MAJOR_REVISION) sang ReviewScore

---

## 2. ⚠️ Review Submission và Editing Deadlines

### 2.1 Deadline Configuration

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **Deadline Entity** có:
  - `type` (DeadlineType enum) - Có `REVIEW` type
  - `dueDate` (LocalDateTime) - Ngày hết hạn
  - `hardDeadline` (Boolean) - Hard/soft deadline flag
  - `description` (String) - Mô tả deadline
- ✅ Deadline được link với Conference (ManyToOne)
- ✅ Multiple deadlines per conference

**Files:**
- `backend/src/main/java/com/uth/confms/conference/entity/Deadline.java`

**Features:**
- ✅ REVIEW deadline type đã được định nghĩa
- ✅ Hard/soft deadline support
- ✅ Deadline được quản lý ở conference level

---

### 2.2 Deadline Enforcement

**Trạng thái:** ⚠️ **Chưa đầy đủ**

**Chi tiết:**
- ✅ **Frontend** check deadline:
  - Frontend ReviewForm check `assignment.deadline`
  - Disable form nếu deadline đã qua
  - Show warning message nếu deadline đã qua
- ⚠️ **Backend ReviewService** KHÔNG check deadline:
  - `createOrUpdateDraft()` không check deadline
  - `submitReview()` không check deadline
  - Chỉ check status (DRAFT vs SUBMITTED)
  - Chỉ check required fields (summary, comments)

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/ReviewService.java`
- `frontend/src/components/review/ReviewForm.tsx`

**Vấn đề:**
- ⚠️ Deadline check chỉ ở frontend, không có backend validation
- ⚠️ User có thể bypass frontend check và submit review sau deadline qua API
- ⚠️ Không có check REVIEW deadline từ Conference deadlines

**Khuyến nghị:**
1. Thêm deadline check vào `ReviewService.createOrUpdateDraft()`:
   - Lấy REVIEW deadline từ conference
   - Check nếu deadline đã qua và là hard deadline
   - Throw BusinessException nếu deadline đã qua
2. Thêm deadline check vào `ReviewService.submitReview()`:
   - Tương tự như trên
   - Prevent submit sau deadline
3. Thêm deadline check vào `DiscussionService.addInternalComment()`:
   - Check REVIEW deadline cho internal comments
4. Thêm deadline validation cho editing (chỉ cho phép edit trước deadline)

---

## 3. ✅ Internal Discussion Visibility Rules

### 3.1 Review Comments Structure

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **ReviewComment Entity** có:
  - `submissionId` (Long) - ID của submission
  - `reviewerId` (Long) - ID của reviewer
  - `content` (TEXT) - Nội dung comment
  - `isInternal` (Boolean, default true) - Internal discussion flag
  - `createdAt`, `updatedAt` - Timestamps
- ✅ Comments được link với submission (không phải review)

**Files:**
- `backend/src/main/java/com/uth/confms/review/entity/ReviewComment.java`

**Features:**
- ✅ Proper entity structure
- ✅ Internal flag để distinguish internal/external comments
- ✅ Timestamps cho tracking

---

### 3.2 Visibility Rules

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **POST /api/reviews/submission/{id}/comments** - Thêm internal comment
  - Authorization: Chỉ PC/REVIEWER
  - Validation: Reviewer phải là PC member của conference
  - PC member phải có status ACCEPTED
- ✅ **GET /api/reviews/submission/{id}/comments** - Lấy internal comments
  - Authorization: PC/REVIEWER/CHAIR/ADMIN
  - Visibility: Chỉ PC members, chair, và admin có thể xem
  - Reviewer names được show trong internal comments
- ✅ **Authors KHÔNG thể xem internal comments:**
  - Check authorization trong `DiscussionService.getInternalComments()`
  - Throw UnauthorizedException nếu không phải PC/chair/admin

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/DiscussionService.java`
- `backend/src/main/java/com/uth/confms/review/controller/ReviewController.java`

**Features:**
- ✅ Proper authorization checks
- ✅ Internal comments chỉ visible cho PC members, chair, admin
- ✅ Reviewer names được show trong internal discussion
- ✅ Authors không thể xem internal comments

---

## 4. ✅ Double-Blind Anonymity Enforcement

### 4.1 Review Mode Configuration

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **Conference Entity** có:
  - `reviewMode` (ReviewMode enum) - SINGLE_BLIND hoặc DOUBLE_BLIND
- ✅ **ReviewMode enum:**
  - `SINGLE_BLIND` - Reviewer biết author, author không biết reviewer
  - `DOUBLE_BLIND` - Cả hai đều không biết nhau

**Files:**
- `backend/src/main/java/com/uth/confms/conference/entity/Conference.java`

**Features:**
- ✅ Review mode được config ở conference level
- ✅ Support cả single-blind và double-blind

---

### 4.2 Anonymity Enforcement

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **shouldShowReviewerName() method** trong ReviewService:
  - Check review mode từ conference
  - Single-blind: Chỉ chair/admin có thể thấy reviewer name
  - Double-blind: Chỉ chair/admin có thể thấy reviewer name
  - Default to double-blind behavior nếu có lỗi
- ✅ **ReviewResponseDTO** có:
  - `reviewerName` (String, nullable) - Chỉ được set nếu `showReviewerName = true`
  - `reviewerId` (Long) - Luôn có, nhưng không reveal identity
- ✅ **Visibility rules:**
  - Reviewer có thể thấy tên của chính mình (own review)
  - Chair/admin luôn có thể thấy reviewer names
  - Authors không thể thấy reviewer names trong double-blind mode
  - Authors có thể thấy reviewer names trong single-blind mode (theo logic hiện tại, nhưng code không implement đúng)

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/ReviewService.java`
- `backend/src/main/java/com/uth/confms/review/dto/ReviewResponseDTO.java`

**Code:**
```java
private boolean shouldShowReviewerName(Long submissionId, Long userId, boolean isChairOrAdmin) {
  Conference.ReviewMode reviewMode = conference.getReviewMode();
  
  if (reviewMode == Conference.ReviewMode.SINGLE_BLIND) {
    // In single-blind, author can see reviewer name
    // Chair/admin can always see reviewer name
    return isChairOrAdmin;
  } else {
    // Double-blind: only chair/admin can see reviewer name
    return isChairOrAdmin;
  }
}
```

**Vấn đề:**
- ⚠️ Single-blind logic không đúng: Comment nói "author can see reviewer name" nhưng code chỉ return `isChairOrAdmin`
- ⚠️ Cần check nếu user là author của submission để show reviewer name trong single-blind mode

**Khuyến nghị:**
1. Fix single-blind logic:
   ```java
   if (reviewMode == Conference.ReviewMode.SINGLE_BLIND) {
     // In single-blind, author can see reviewer name
     boolean isAuthor = submission.getAuthorId().equals(userId);
     return isAuthor || isChairOrAdmin;
   }
   ```

---

## 5. ✅ Review Workflow

### 5.1 Review Creation và Editing

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **POST /api/reviews/draft** - Tạo/cập nhật draft review
  - Authorization: PC/REVIEWER only
  - Validation: Reviewer phải là owner của assignment
  - Assignment phải có status ACCEPTED
  - Review được tạo với status DRAFT
  - Có thể update draft review nhiều lần
- ✅ **Review Status:**
  - `DRAFT` - Đang soạn thảo, có thể edit
  - `SUBMITTED` - Đã submit, không thể edit

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/ReviewService.java`
- `backend/src/main/java/com/uth/confms/review/controller/ReviewController.java`

**Features:**
- ✅ Proper authorization checks
- ✅ Status validation (chỉ DRAFT có thể edit)
- ✅ Assignment status validation
- ✅ Draft review có thể được update nhiều lần

---

### 5.2 Review Submission

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **POST /api/reviews/{id}/submit** - Submit review
  - Authorization: PC/REVIEWER only
  - Validation: Reviewer phải là owner của review
  - Status check: Chỉ DRAFT có thể submit
  - Required fields: summary và comments phải có
  - Update review status to SUBMITTED
  - Update assignment status to COMPLETED
  - Set submittedAt timestamp

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/ReviewService.java`

**Features:**
- ✅ Comprehensive validation
- ✅ Status transition (DRAFT → SUBMITTED)
- ✅ Assignment status update
- ✅ Timestamp tracking

**Có thể cải thiện:**
- ⚠️ Không có deadline check (đã đề cập ở section 2.2)

---

### 5.3 Review Retrieval

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **GET /api/reviews/assignment/{id}** - Lấy review của assignment (own review)
  - Authorization: PC/REVIEWER only
  - Reviewer có thể thấy tên của chính mình
- ✅ **GET /api/reviews/submission/{id}** - Lấy reviews của submission
  - Authorization: Authenticated users
  - Anonymity được enforce dựa trên review mode
  - Reviewer names chỉ được show nếu allowed
- ✅ **GET /api/reviews/{id}** - Lấy review by ID
  - Authorization: Reviewer (own review) hoặc chair/admin
  - Anonymity được enforce

**Features:**
- ✅ Multiple retrieval endpoints
- ✅ Proper authorization checks
- ✅ Anonymity enforcement

---

## 6. ✅ Rebuttal Support

### 6.1 Rebuttal Structure

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **Rebuttal Entity** có:
  - `submissionId` (Long) - ID của submission
  - `authorId` (Long) - ID của author
  - `content` (TEXT) - Nội dung rebuttal
  - `status` (RebuttalStatus enum) - DRAFT hoặc SUBMITTED
  - `createdAt`, `submittedAt` - Timestamps
- ✅ Rebuttal được link với submission (OneToOne)

**Files:**
- `backend/src/main/java/com/uth/confms/review/entity/Rebuttal.java`

**Features:**
- ✅ Proper entity structure
- ✅ Status management (DRAFT/SUBMITTED)
- ✅ Timestamps cho tracking

---

### 6.2 Rebuttal Workflow

**Trạng thái:** ✅ **Hoàn thiện**

**Chi tiết:**
- ✅ **POST /api/reviews/rebuttal** - Tạo/cập nhật rebuttal
  - Authorization: AUTHOR only
  - Validation: Chỉ author của submission có thể tạo rebuttal
  - Rebuttal được tạo với status DRAFT
  - Có thể update draft rebuttal
- ✅ **POST /api/reviews/rebuttal/{id}/submit** - Submit rebuttal
  - Authorization: AUTHOR only
  - Status check: Chỉ DRAFT có thể submit
  - Update status to SUBMITTED
- ✅ **GET /api/reviews/rebuttal/submission/{id}** - Lấy rebuttal
  - Authorization: Author, PC members, chair, admin
  - Visibility: Author, reviewers, chair, admin có thể xem

**Files:**
- `backend/src/main/java/com/uth/confms/review/service/DiscussionService.java`
- `backend/src/main/java/com/uth/confms/review/controller/ReviewController.java`

**Features:**
- ✅ Proper authorization checks
- ✅ Status management
- ✅ Visibility rules

---

## 7. 📊 Đánh Giá Tổng Thể

### 7.1 Điểm Mạnh

1. ✅ **Review Workflow:** Hoàn thiện với draft/submit flow, status management
2. ✅ **Scoring Criteria:** Comprehensive scoring scale (STRONG_ACCEPT to STRONG_REJECT)
3. ✅ **Double-Blind Anonymity:** Proper enforcement với review mode configuration
4. ✅ **Internal Discussion:** Proper visibility rules, chỉ PC members/chair/admin có thể xem
5. ✅ **Rebuttal Support:** Hoàn thiện với author rebuttal workflow
6. ✅ **Authorization:** Proper authorization checks ở tất cả endpoints

### 7.2 Điểm Yếu

1. ⚠️ **Form Structure Mismatch:** Frontend và backend không khớp (overallRating, confidence, recommendation)
2. ⚠️ **Deadline Enforcement:** Deadline check chỉ ở frontend, không có backend validation
3. ⚠️ **Single-Blind Logic:** Code không implement đúng single-blind logic (author không thể thấy reviewer name)
4. ⚠️ **Numeric Score:** Không có numeric score để tính average score

### 7.3 Mức Độ Hoàn Thiện

**Tổng thể:** ✅ **75% Hoàn thiện**

- ✅ Review Workflow: 100%
- ✅ Scoring Criteria: 90% (thiếu numeric score)
- ✅ Double-Blind Anonymity: 80% (single-blind logic chưa đúng)
- ⚠️ Deadline Enforcement: 50% (chỉ có frontend check)
- ⚠️ Form Structure: 60% (không khớp giữa frontend và backend)
- ✅ Internal Discussion: 100%
- ✅ Rebuttal Support: 100%

---

## 8. 📋 Khuyến Nghị Hành Động

### 8.1 Ưu Tiên Cao

1. **Fix Form Structure Mismatch:**
   - Thêm `overallRating` (Integer, 1-5) vào Review entity hoặc xóa khỏi frontend
   - Thêm `confidence` (Integer, 1-5) vào Review entity hoặc xóa khỏi frontend
   - Map `recommendation` từ frontend sang `score` trong backend (hoặc thêm recommendation field)
   - Đảm bảo frontend và backend đồng bộ

2. **Thêm Deadline Enforcement vào Backend:**
   - Thêm deadline check vào `ReviewService.createOrUpdateDraft()`
   - Thêm deadline check vào `ReviewService.submitReview()`
   - Lấy REVIEW deadline từ conference deadlines
   - Throw BusinessException nếu deadline đã qua và là hard deadline

3. **Fix Single-Blind Logic:**
   - Update `shouldShowReviewerName()` để check nếu user là author
   - Trong single-blind mode, author có thể thấy reviewer name
   - Trong double-blind mode, chỉ chair/admin có thể thấy reviewer name

### 8.2 Ưu Tiên Trung Bình

4. **Thêm Numeric Score:**
   - Thêm `numericScore` (Integer, 1-7) vào Review entity
   - Map ReviewScore enum sang numeric score (STRONG_ACCEPT = 7, STRONG_REJECT = 1)
   - Tính average score cho submission
   - Thêm endpoint để lấy average score

5. **Thêm Deadline Validation cho Editing:**
   - Prevent editing sau deadline
   - Check deadline khi update draft review
   - Check deadline khi add internal comment

### 8.3 Ưu Tiên Thấp

6. **Thêm Review Statistics:**
   - Review completion rate
   - Average review score per submission
   - Review submission timeline
   - Reviewer performance metrics

7. **Thêm Review Templates:**
   - Pre-defined review templates
   - Template selection khi create review
   - Template customization

---

## 9. 📝 Kết Luận

Module Review đã được triển khai **khá tốt** với:

- ✅ **Review Workflow:** Hoàn thiện với draft/submit flow và status management
- ✅ **Scoring Criteria:** Comprehensive scoring scale
- ✅ **Double-Blind Anonymity:** Proper enforcement với review mode configuration
- ✅ **Internal Discussion:** Proper visibility rules
- ✅ **Rebuttal Support:** Hoàn thiện với author rebuttal workflow
- ⚠️ **Form Structure:** Không khớp giữa frontend và backend
- ⚠️ **Deadline Enforcement:** Chỉ có frontend check, thiếu backend validation
- ⚠️ **Single-Blind Logic:** Code không implement đúng single-blind logic

**Vấn đề chính cần xử lý:**
1. Form structure mismatch (🔴 CAO)
2. Deadline enforcement (🔴 CAO)
3. Single-blind logic (🔴 CAO)
4. Numeric score (🟡 TRUNG BÌNH)

**Sau khi xử lý các vấn đề trên, module Review sẽ đạt 90%+ hoàn thiện.**

---

**Báo cáo được tạo bởi:** AI Assistant  
**Ngày:** $(date)
