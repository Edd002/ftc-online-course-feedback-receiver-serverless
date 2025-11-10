package fiap.tech.challenge.online.course.feedback.receiver.serverless.dao;

import fiap.tech.challenge.online.course.feedback.receiver.serverless.config.CryptoConfig;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.config.DataSourceConfig;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.FeedbackRequest;
import fiap.tech.challenge.online.course.feedback.receiver.serverless.payload.FeedbackResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class FTCOnlineCourseFeedbackReceiverServerlessDAO {

    private final Connection connection;

    public FTCOnlineCourseFeedbackReceiverServerlessDAO() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
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

    public Long getUserIdByEmailAndAccessKey(FeedbackRequest feedbackRequest) {
        try {
            PreparedStatement preparedStatement = switch (feedbackRequest.userType()) {
                case ADMINISTRATOR -> connection.prepareStatement("SELECT id FROM t_administrator WHERE email = ? AND access_key = ?");
                case TEACHER -> connection.prepareStatement("SELECT id FROM t_teacher WHERE email = ? AND access_key = ?");
                case STUDENT -> connection.prepareStatement("SELECT id FROM t_student WHERE email = ? AND access_key = ?");
            };
            preparedStatement.setString(1, feedbackRequest.email());
            preparedStatement.setString(2, new CryptoConfig().encrypt(feedbackRequest.accessKey()));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new NoSuchElementException("Nenhum usuário encontrado com as credenciais infommadas foi encontrado para realizar a busca de feedbacks.");
            }
            return resultSet.getLong("id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FeedbackResponse> getFeedbackResponse(Long userId, FeedbackRequest feedbackRequest) {
        List<FeedbackResponse> feedbackResponseList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = switch (feedbackRequest.userType()) {
                case ADMINISTRATOR -> preparedStatementAdministrator(connection, userId, feedbackRequest);
                case TEACHER -> preparedStatementTeacher(connection, userId, feedbackRequest);
                case STUDENT -> preparedStatementStudent(connection, userId, feedbackRequest);
            };
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                feedbackResponseList.add(new FeedbackResponse(
                        resultSet.getBoolean("urgent"),
                        resultSet.getString("description"),
                        resultSet.getString("comment"),
                        resultSet.getString("student_name"),
                        resultSet.getString("student_email")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return feedbackResponseList;
    }

    private PreparedStatement preparedStatementAdministrator(Connection connection, Long userId, FeedbackRequest feedbackRequest) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT tf.urgent as urgent, tf.description as description, tf.comment as comment, ts.name as student_name, ts.email as student_email FROM public.t_feedback tf " +
                        "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                        "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                        "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                        "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                        "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                        "WHERE tadmin.id = ? " +
                        "AND (? IS NULL OR tf.urgent = ?) AND (? IS NULL OR tf.description LIKE CONCAT( '%', ?, '%')) AND (? IS NULL OR tf.comment LIKE CONCAT( '%', ?, '%'));");
        return setPreparedStatementParameters(userId, feedbackRequest, preparedStatement);
    }

    private PreparedStatement preparedStatementTeacher(Connection connection, Long userId, FeedbackRequest feedbackRequest) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT tf.urgent as urgent, tf.description as description, tf.comment as comment, ts.name as student_name, ts.email as student_email FROM public.t_feedback tf " +
                        "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                        "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                        "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                        "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                        "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                        "WHERE tt.id = ? " +
                        "AND (? IS NULL OR tf.urgent = ?) AND (? IS NULL OR tf.description LIKE CONCAT( '%', ?, '%')) AND (? IS NULL OR tf.comment LIKE CONCAT( '%', ?, '%'));");
        return setPreparedStatementParameters(userId, feedbackRequest, preparedStatement);
    }

    private PreparedStatement preparedStatementStudent(Connection connection, Long userId, FeedbackRequest feedbackRequest) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT tf.urgent as urgent, tf.description as description, tf.comment as comment, ts.name as student_name, ts.email as student_email FROM public.t_feedback tf " +
                        "INNER JOIN public.t_assessment ta on ta.id = tf.fk_assessment " +
                        "INNER JOIN public.t_teacher_student tts on tts.id = ta.fk_teacher_student " +
                        "INNER JOIN public.t_teacher tt on tt.id = tts.fk_teacher " +
                        "INNER JOIN public.t_student ts on ts.id = tts.fk_student " +
                        "INNER JOIN public.t_administrator tadmin on tadmin.id = tt.fk_administrator " +
                        "WHERE ts.id = ? " +
                        "AND (? IS NULL OR tf.urgent = ?) AND (? IS NULL OR tf.description LIKE CONCAT( '%', ?, '%')) AND (? IS NULL OR tf.comment LIKE CONCAT( '%', ?, '%'));");
        return setPreparedStatementParameters(userId, feedbackRequest, preparedStatement);
    }

    private PreparedStatement setPreparedStatementParameters(Long userId, FeedbackRequest feedbackRequest, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, userId);
        if (feedbackRequest.urgent() == null) {
            preparedStatement.setNull(2, Types.BOOLEAN);
            preparedStatement.setNull(3, Types.BOOLEAN);
        } else {
            preparedStatement.setBoolean(2, feedbackRequest.urgent());
            preparedStatement.setBoolean(3, feedbackRequest.urgent());
        }
        preparedStatement.setString(4, feedbackRequest.description());
        preparedStatement.setString(5, feedbackRequest.description());
        preparedStatement.setString(6, feedbackRequest.comment());
        preparedStatement.setString(7, feedbackRequest.comment());
        return preparedStatement;
    }
}