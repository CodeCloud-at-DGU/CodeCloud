package dao;

import model.Post;
import model.Group;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    // 게시물을 데이터베이스에 저장하는 메서드 - 저장 잘 됨
    public Long savePost(Post post) throws SQLException {
        String sql = "INSERT INTO Posts (writer, group_id, title, content) VALUES (?, ?, ?, ?)";
        Long postId = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, post.getWriter());
            stmt.setLong(2, 5L);
            stmt.setString(3, post.getTitle());
            stmt.setString(4, post.getContent());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        postId = generatedKeys.getLong(1);
                    }
                }
            }
        }
        return postId;
    }
    
    // 특정 ID의 게시물을 찾는 메서드
    public Post getPostById(int postId) throws SQLException {
        String sql = "SELECT * FROM Posts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setWriter(rs.getLong("writer"));
                post.setGroupId(rs.getLong("group_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                post.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());
                return post;
            }
        }
		return null;
   
    }
    
    // 사용자의 닉네임을 가져오는 메서드
    public String findNicknameByUserId(Long userId) throws SQLException {
        String nickname = null;
        String sql = "SELECT nickname FROM Users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nickname = rs.getString("nickname");
            }
        }
        return nickname;
    }   
    
    // 사용자가 가입한 그룹의 목록을 가져오는 메서드
    public List<Group> getGroupsByUserId(Long userId) throws SQLException {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.groupId, g.groupName FROM Groups g " +
                     "INNER JOIN UserGroups ug ON g.groupId = ug.groupId " +
                     "WHERE ug.userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Group group = new Group();
                    group.setGroupId(rs.getLong("groupId"));
                    group.setGroupName(rs.getString("groupName"));
                    groups.add(group);
                }
            }
        }
        return groups;
    }
    
    // POST ID로 Group Name가져오기 
    public String findGroupNameByPostId(int postId) throws SQLException {

        String groupName = null;
        int groupId = 0; // INT로 변경

        // postId에서 groupId를 가져오는 쿼리문 추가
        String groupIdQuery = "SELECT group_id FROM Posts WHERE id = ?";
        String groupNameQuery = "SELECT groupName FROM `Groups` WHERE groupId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement groupIdStmt = conn.prepareStatement(groupIdQuery);
             PreparedStatement groupNameStmt = conn.prepareStatement(groupNameQuery)) {

            // postId로 groupId를 가져오기 위한 쿼리 실행
            groupIdStmt.setInt(1, postId); // INT로 변경
            ResultSet groupIdResultSet = groupIdStmt.executeQuery();
            
            if (groupIdResultSet.next()) {
                groupId = groupIdResultSet.getInt("group_id"); // INT로 변경
            }
            
            // 중간 진행 상황 출력
            System.out.println("🤖 진행중 - groupId 가져오기 완료");

            if (groupId != 0) { // 0과 비교하여 NULL 검사(INT 형식에서는 NULL 대신 0 사용)
                // groupId로 groupName을 가져오기 위한 쿼리 실행
                groupNameStmt.setInt(1, groupId); // INT로 변경
                ResultSet groupNameResultSet = groupNameStmt.executeQuery();

                if (groupNameResultSet.next()) {
                    groupName = groupNameResultSet.getString("groupName");
                } else {
                    // groupName을 찾지 못한 경우
                    System.err.println("❌ groupName을 찾지 못했습니다.");
                }
            } else {
                // groupId를 찾지 못한 경우
                System.err.println("❌ groupId를 찾지 못했습니다.");
            }
            
            // 중간 진행 상황 출력
            System.out.println("🤖 진행중 - groupName 가져오기 완료");
        } catch (SQLException e) {
            // 데이터베이스 관련 예외 처리
            System.err.println("❌ 데이터베이스 오류: " + e.getMessage());
            throw e; // 예외를 다시 던져서 상위 호출자에게 처리를 위임
        }

        return groupName;
    }


    
 //
    
    
    
    // 사용자가 속한 그룹의 ID를 가져오는 메서드
    public Long getGroupIdByUserId(Long userId) throws SQLException {
        Long groupId = null;
        String QUERY_GROUPID = "SELECT groupId FROM `UserGroups` WHERE userId = ?"; // 가정한 테이블 및 컬럼명

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY_GROUPID)) {
             
            preparedStatement.setLong(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    groupId = resultSet.getLong("groupId");
                }
            }
        }
        return groupId;
    }
    
    // 그룹 리스트
    public List<Post> getAllPosts() throws SQLException {
        List<Post> allPosts = new ArrayList<>();
        String sql = "SELECT * FROM `Posts`";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setWriter(rs.getLong("writer"));
                post.setGroupId(rs.getLong("group_id"));
                post.setTitle(rs.getString("title"));
                post.setContent(rs.getString("content"));
                post.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                post.setUpdatedAt(rs.getTimestamp("updatedt").toLocalDateTime());
                allPosts.add(post);
            }
        }
        return allPosts;
    }

}
