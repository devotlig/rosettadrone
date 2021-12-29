/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE ATTITUDE_TARGET PACKING
package com.MAVLink.common;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
 * Reports the current commanded attitude of the vehicle as specified by the autopilot. This should match the commands sent in a SET_ATTITUDE_TARGET message if the vehicle is being controlled this way.
 */
public class msg_attitude_target extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_ATTITUDE_TARGET = 83;
    public static final int MAVLINK_MSG_LENGTH = 37;
    private static final long serialVersionUID = MAVLINK_MSG_ID_ATTITUDE_TARGET;


    /**
     * Timestamp (time since system boot).
     */
    public long time_boot_ms;

    /**
     * Attitude quaternion (w, x, y, z order, zero-rotation is 1, 0, 0, 0)
     */
    public float q[] = new float[4];

    /**
     * Body roll rate
     */
    public float body_roll_rate;

    /**
     * Body pitch rate
     */
    public float body_pitch_rate;

    /**
     * Body yaw rate
     */
    public float body_yaw_rate;

    /**
     * Collective thrust, normalized to 0 .. 1 (-1 .. 1 for vehicles capable of reverse trust)
     */
    public float thrust;

    /**
     * Mappings: If any of these bits are set, the corresponding input should be ignored: bit 1: body roll rate, bit 2: body pitch rate, bit 3: body yaw rate. bit 4-bit 7: reserved, bit 8: attitude
     */
    public short type_mask;


    /**
     * Generates the payload for a mavlink message for a message of this type
     *
     * @return
     */
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_ATTITUDE_TARGET;

        packet.payload.putUnsignedInt(time_boot_ms);


        for (int i = 0; i < q.length; i++) {
            packet.payload.putFloat(q[i]);
        }


        packet.payload.putFloat(body_roll_rate);

        packet.payload.putFloat(body_pitch_rate);

        packet.payload.putFloat(body_yaw_rate);

        packet.payload.putFloat(thrust);

        packet.payload.putUnsignedByte(type_mask);

        return packet;
    }

    /**
     * Decode a attitude_target message into this class fields
     *
     * @param payload The message to decode
     */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();

        this.time_boot_ms = payload.getUnsignedInt();


        for (int i = 0; i < this.q.length; i++) {
            this.q[i] = payload.getFloat();
        }


        this.body_roll_rate = payload.getFloat();

        this.body_pitch_rate = payload.getFloat();

        this.body_yaw_rate = payload.getFloat();

        this.thrust = payload.getFloat();

        this.type_mask = payload.getUnsignedByte();

    }

    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_attitude_target() {
        msgid = MAVLINK_MSG_ID_ATTITUDE_TARGET;
    }

    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     */
    public msg_attitude_target(MAVLinkPacket mavLinkPacket) {
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_ATTITUDE_TARGET;
        unpack(mavLinkPacket.payload);
    }


    /**
     * Returns a string with the MSG name and data
     */
    public String toString() {
        return "MAVLINK_MSG_ID_ATTITUDE_TARGET - sysid:" + sysid + " compid:" + compid + " time_boot_ms:" + time_boot_ms + " q:" + q + " body_roll_rate:" + body_roll_rate + " body_pitch_rate:" + body_pitch_rate + " body_yaw_rate:" + body_yaw_rate + " thrust:" + thrust + " type_mask:" + type_mask + "";
    }
}
        