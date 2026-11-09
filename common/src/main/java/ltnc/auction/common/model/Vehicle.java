package ltnc.auction.common.model;

import java.time.Instant;

import ltnc.auction.common.util.Validators;

/**
 * A vehicle put up for auction, such as a car or a motorbike.
 */
public class Vehicle extends Item {

    private static final int MIN_MILEAGE_KM = 0;

    // Upper bound of a realistic odometer reading (a decision of the team).
    private static final int MAX_MILEAGE_KM = 9_999_999;

    // Keep in sync with the column that stores the plate number.
    private static final int MAX_PLATE_NUMBER_LENGTH = 20;

    private int mileageKm;
    private String plateNo;

    /**
     * Creates a vehicle.
     *
     * <p>Pass {@code null} for {@code id}, {@code createdAt} and {@code updatedAt} to create a new
     * item. Pass the stored values to restore an item loaded from the database.
     *
     * @param id          the database id, or {@code null} for a new item
     * @param createdAt   the creation time, or {@code null} for a new item
     * @param updatedAt   the last update time, or {@code null} for a new item
     * @param name        the item name, must not be blank
     * @param description the item description, may be {@code null}
     * @param sellerId    the id of the seller who owns this item, must be positive
     * @param mileageKm   the distance travelled in kilometres, from {@value #MIN_MILEAGE_KM} to
     *                    {@value #MAX_MILEAGE_KM}
     * @param plateNo     the licence plate number, must not be blank
     * @throws IllegalArgumentException if any value is invalid
     */
    public Vehicle(
            Long id,
            Instant createdAt,
            Instant updatedAt,
            String name,
            String description,
            long sellerId,
            int mileageKm,
            String plateNo) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.mileageKm = validMileageKm(mileageKm);
        this.plateNo = validPlateNo(plateNo);
    }

    public int getMileageKm() {
        return mileageKm;
    }

    public String getPlateNo() {
        return plateNo;
    }

    /**
     * Changes the mileage and plate number and refreshes the update time.
     *
     * <p>Both values are validated before either is assigned, so a failed call leaves this item
     * unchanged.
     *
     * @param mileageKm the new distance travelled in kilometres
     * @param plateNo   the new licence plate number, must not be blank
     * @throws IllegalArgumentException if a value is invalid
     */
    public void updateDetails(int mileageKm, String plateNo) {
        int newMileageKm = validMileageKm(mileageKm);
        String newPlateNo = validPlateNo(plateNo);
        this.mileageKm = newMileageKm;
        this.plateNo = newPlateNo;
        touch();
    }

    @Override
    public ItemType getCategory() {
        return ItemType.VEHICLE;
    }

    @Override
    public String getInfo() {
        return String.format(
                "[VEHICLE] %s - mileage: %dkm (Plate Number: %s)", getName(), mileageKm, plateNo);
    }

    private static int validMileageKm(int mileageKm) {
        return Validators.requireInRange(mileageKm, MIN_MILEAGE_KM, MAX_MILEAGE_KM, "mileageKm");
    }

    private static String validPlateNo(String plateNo) {
        String trimmed = Validators.requireNotBlank(plateNo, "plateNo");
        return Validators.requireMaxLength(trimmed, MAX_PLATE_NUMBER_LENGTH, "plateNo");
    }
}