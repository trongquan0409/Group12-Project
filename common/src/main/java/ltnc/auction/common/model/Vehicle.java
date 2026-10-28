package ltnc.auction.common.model;
import java.time.Instant;

public class Vehicle extends Item{
    private int mileageKm;
    private int plateNo;

    public Vehicle(Long id, Instant createdAt, Instant updatedAt, String name, String description, long sellerId, int mileageKm, int plateNo) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.updateDetails(mileageKm, plateNo);
    }

    public int getMileageKm() { return mileageKm; }
    public int getPlateNo() { return plateNo; }

    public void updateDetails(int mileageKm, int plateNo) {
        if (mileageKm < 0) throw new IllegalArgumentException("Illegal mileageKm");
        this.mileageKm = mileageKm;
        if (plateNo < 0) throw new IllegalArgumentException("Illegal plateNo");
        this.plateNo = plateNo;
        super.touch();
    }

    @Override
    public ItemType getCategory() { return ItemType.VEHICLE; }
    @Override
    public String getInfo() {
        return String.format("[VEHICLE] %s - mileage: %dkm (Plate Number: %d)", getName(), mileageKm, plateNo);
    }
}
