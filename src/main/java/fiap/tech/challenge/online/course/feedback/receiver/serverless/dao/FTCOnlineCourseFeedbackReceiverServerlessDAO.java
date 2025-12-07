package fiap.tech.challenge.online.course.feedback.receiver.serverless.dao;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.config.CryptoConfig;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.config.DataSourceConfig;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.record.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.enumeration.UserType;

import java.sql.*;
import java.util.NoSuchElementException;
import java.util.Properties;

public class FTCOnlineCourseFeedbackReceiverServerlessDAO {

    private final Connection connection;

    public FTCOnlineCourseFeedbackReceiverServerlessDAO(Properties applicationProperties) {
        DataSourceConfig dataSourceConfig = new DataSourceConfig(applicationProperties);
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(dataSourceConfig.getJdbcUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
            if (!connection.isValid(0)) {
                throw new SQLException("Não foi possível estabelecer uma conexão com o banco de dados. URL de conexão: " + connection.getMetaData().getURL());
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getTeacherIdByEmailAndAccessKey(FeedbackRequest feedbackRequest) {
        try {
            if (!feedbackRequest.userType().equals(UserType.TEACHER)) {
                throw new IllegalStateException("Somente usuários do tipo professor podem cadastrar feedback de alunos.");
            }
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM t_teacher WHERE email = ? AND access_key = ?");
            preparedStatement.setString(1, feedbackRequest.email());
            preparedStatement.setString(2, new CryptoConfig().encrypt(feedbackRequest.accessKey()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new NoSuchElementException("Nenhum professor encontrado com as credenciais infommadas foi encontrado para realizar o cadastro de feedback.");
            }
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getTeacherStudentIdByTeacherIdAndStudentEmail(Long teacherId, FeedbackRequest feedbackRequest) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT tts.id FROM t_teacher_student tts WHERE tts.fk_teacher = ? AND tts.fk_student = (SELECT ts.id FROM t_student ts WHERE ts.email = ?);");
            preparedStatement.setLong(1, teacherId);
            preparedStatement.setString(2, feedbackRequest.studentEmail());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new NoSuchElementException("Nenhum vínculo de professor com o aluno foi encontrado para realizar o cadastro de feedback.");
            }
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerFeedback(Long teacherStudentId, FeedbackRequest feedbackRequest) {
        try {
            PreparedStatement preparedStatement = preparedStatement(connection, teacherStudentId, feedbackRequest);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected < 1) {
                throw new SQLException("Ocorreu um problema ao cadastrar o feedback. Tente novamente mais tarde.");
            }
            if (feedbackRequest.feedbackUrgent()) {
                // CALL ftc-online-course-report-serverless TO SEND E-MAIL TO ADMINISTRATOR
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement preparedStatement(Connection connection, Long teacherStudentId, FeedbackRequest feedbackRequest) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "WITH new_assessment AS (" +
                        "    INSERT INTO t_assessment(id, created_by, name, type, score, fk_teacher_student) " +
                        "    VALUES (nextval('sq_assessment'), ?, ?, ?, ?, ?) " +
                        "    RETURNING id AS assessment_id " +
                        ")" +
                        "INSERT INTO t_feedback(id, created_by, urgent, description, comment, fk_assessment) " +
                        "SELECT nextval('sq_feedback'), ?, ?, ?, ?, assessment_id " +
                        "FROM new_assessment;");
        return setPreparedStatementParameters(teacherStudentId, feedbackRequest, preparedStatement);
    }

    private PreparedStatement setPreparedStatementParameters(Long teacherStudentId, FeedbackRequest feedbackRequest, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, feedbackRequest.email());
        preparedStatement.setString(2, feedbackRequest.assessmentName());
        preparedStatement.setString(3, feedbackRequest.assessmentType().name());
        preparedStatement.setDouble(4, feedbackRequest.assessmentScore());
        preparedStatement.setLong(5, teacherStudentId);
        preparedStatement.setString(6, feedbackRequest.email());
        preparedStatement.setBoolean(7, feedbackRequest.feedbackUrgent());
        preparedStatement.setString(8, feedbackRequest.feedbackDescription());
        preparedStatement.setString(9, feedbackRequest.feedbackComment());
        return preparedStatement;
    }
}