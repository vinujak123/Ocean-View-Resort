package com.oceanview.resort.repository;

import com.oceanview.resort.model.Reservation;
import com.oceanview.resort.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlReservationRepository implements ReservationRepository {

    @Override
    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Reservation save(Reservation res) {
        if (res.getId() == null) {
            return insert(res);
        } else {
            return update(res);
        }
    }

    private Reservation insert(Reservation res) {
        String sql = "INSERT INTO reservations (reference_id, guest_name, address, phone, room_type, board_type, check_in_date, check_out_date, total_bill) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, res.getReferenceId());
            pstmt.setString(2, res.getGuestName());
            pstmt.setString(3, res.getAddress());
            pstmt.setString(4, res.getPhone());
            pstmt.setString(5, res.getRoomType().name());
            pstmt.setString(6, res.getBoardType().name());
            pstmt.setDate(7, Date.valueOf(res.getCheckInDate()));
            pstmt.setDate(8, Date.valueOf(res.getCheckOutDate()));
            pstmt.setDouble(9, res.getTotalBill());

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    res.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    private Reservation update(Reservation res) {
        String sql = "UPDATE reservations SET reference_id=?, guest_name=?, address=?, phone=?, room_type=?, board_type=?, check_in_date=?, check_out_date=?, total_bill=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, res.getReferenceId());
            pstmt.setString(2, res.getGuestName());
            pstmt.setString(3, res.getAddress());
            pstmt.setString(4, res.getPhone());
            pstmt.setString(5, res.getRoomType().name());
            pstmt.setString(6, res.getBoardType().name());
            pstmt.setDate(7, Date.valueOf(res.getCheckInDate()));
            pstmt.setDate(8, Date.valueOf(res.getCheckOutDate()));
            pstmt.setDouble(9, res.getTotalBill());
            pstmt.setLong(10, res.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Optional<Reservation> findByReferenceId(String referenceId) {
        String sql = "SELECT * FROM reservations WHERE reference_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, referenceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public String findMaxReferenceId() {
        String sql = "SELECT MAX(CAST(reference_id AS UNSIGNED)) FROM reservations WHERE reference_id REGEXP '^[0-9]+$'";
        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String max = rs.getString(1);
                return max;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getLong("id"));
        r.setReferenceId(rs.getString("reference_id"));
        r.setGuestName(rs.getString("guest_name"));
        r.setAddress(rs.getString("address"));
        r.setPhone(rs.getString("phone"));
        r.setRoomType(Reservation.RoomType.valueOf(rs.getString("room_type")));
        r.setBoardType(Reservation.BoardType.valueOf(rs.getString("board_type")));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setTotalBill(rs.getDouble("total_bill"));
        return r;
    }
}
