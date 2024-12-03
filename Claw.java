package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Claw {
    private LinearOpMode opmode;
    // private Servo clawHand;
    // private Servo clawArm;
    private DcMotor rightSlide;
    private DcMotor leftSlide;

    private int eventsPerMotorRotation = 28; 
    private double wheelMotorRatio = 19.2;
    private double eventsPerWheelRotation = eventsPerMotorRotation * wheelMotorRatio;
    private double inchesPerWheelRotation = 4.7244;
    private boolean slideStopped = true;

    // private boolean handClosed = false;
    // private double handClosedPosition = 0.30;
    // private double handOpenPosition = 0.1;

    public enum ArmPosition { Basket, Starting, Verticle, Down }
    private double armPositionNoPower = 0.65;
    private double armStartingStarting = 0.55;
    private double armStartingBasket = 0.30;
    private double armPositionVerticle = 0.5;
    private double armPositionDown = 0.14;
    private double desiredArmPosition = armPositionNoPower;

    public enum SlidePosition { UpperBasket, Bottom, Top }
    private double maxSlidePower = 1;
    private double slideHeightBottom = 0;
    private double slideHeightUpperBasket = 37;
    private double slideHeightTop = 38;
    private double desiredSlideHeight = slideHeightBottom;

    public void init(LinearOpMode opmode, HardwareMap hwMap) {
        this.opmode = opmode;
        // clawHand = hwMap.get(Servo.class, "clawHand");
        // clawArm = hwMap.get(Servo.class, "clawArm");

        rightSlide = hwMap.dcMotor.get("rightSlide");
        rightSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightSlide.setDirection(DcMotorSimple.Direction.FORWARD);
        rightSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftSlide = hwMap.dcMotor.get("leftSlide");
        leftSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftSlide.setDirection(DcMotorSimple.Direction.FORWARD);
        leftSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        SetClawToStartPosition();
    }

    public void PerformClawMovements(double slidePower) {
        if (slidePower == 0) {
            PerformSlideMovements();
        } else {
            SetSlidePower(slidePower, true);
        }
        // PerformArmMovements(); // Removed as it depends on clawArm
    }

    private void SetClawToStartPosition() {
        // clawArm.setPosition(desiredArmPosition); 
        // CloseHand(); 
        rightSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightSlide.setDirection(DcMotorSimple.Direction.REVERSE);
        rightSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftSlide.setDirection(DcMotorSimple.Direction.REVERSE);
        leftSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        SetArmPosition(ArmPosition.Starting);
    }

    private void SetSlidePower(double slidePower, boolean Manual) {
        if (CurrentSlideHeight() > 37.5 && slidePower > 0) {
            slidePower = slidePower / 2;
        }
        if (CurrentSlideHeight() > 38 && slidePower > 0) {
            slidePower = 0;
        }
        if (CurrentSlideHeight() <= 1 && slidePower < 0) {
            slidePower = slidePower / 2;
        }
        if (CurrentSlideHeight() <= 0 && slidePower < 0) {
            slidePower = 0;
        }
        if (slidePower == 0 && CurrentSlideHeight() > 0) {
            if (!slideStopped) {
                rightSlide.setPower(0);
                leftSlide.setPower(0);
                sleep(10);
                rightSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                rightSlide.setTargetPosition(rightSlide.getCurrentPosition());
                rightSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                rightSlide.setPower(0.1);

                leftSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                leftSlide.setTargetPosition(rightSlide.getCurrentPosition());
                leftSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                leftSlide.setPower(0.1);

                slideStopped = true;
            }
        } else {
            slideStopped = false;
            leftSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftSlide.setPower(slidePower);
            rightSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightSlide.setPower(slidePower);
        }
        if (Manual) {
            desiredSlideHeight = CurrentSlideHeight();
        }
    }

    public double CurrentLeftSlidePower() {
        return leftSlide.getPower();
    }

    public double CurrentRightSlidePower() {
        return rightSlide.getPower();
    }

    public double CurrentSlideHeight() {
        return rightSlide.getCurrentPosition() / eventsPerWheelRotation * inchesPerWheelRotation;
    }

    private void PerformSlideMovements() {
        double distanceToMove = desiredSlideHeight - CurrentSlideHeight();
        double motorPowerIncrement = 0.05;
        if (Math.abs(distanceToMove) > 0.1) {
            double neededWheelRotations = distanceToMove / inchesPerWheelRotation;
            double remainingRotations = Math.abs(neededWheelRotations);
            double desiredPower = Math.signum(neededWheelRotations) * DesiredSlidePower(remainingRotations, maxSlidePower);
            double currentPower = rightSlide.getPower();
            double powerDifference = desiredPower - currentPower;
            if (powerDifference > motorPowerIncrement) {
                desiredPower = Math.min(desiredPower, currentPower + motorPowerIncrement);
            } else if (powerDifference < -motorPowerIncrement) {
                desiredPower = Math.max(desiredPower, currentPower - motorPowerIncrement);
            }
            SetSlidePower(desiredPower, false);
        } else {
            SetSlidePower(0, false);
        }
    }

    private double DesiredSlidePower(double remainingRotations, double maxPower) {
        if (remainingRotations <= 0) {
            return 0;
        }
        double desiredPower = Math.min(maxPower, remainingRotations);
        desiredPower = Math.max(desiredPower, 0.2);
        return Math.round(desiredPower * 20.0) / 20.0;
    }

    public void SetArmPosition(ArmPosition armPosition) {
        // Implement functionality without clawArm if necessary
    }

    public void SetSlideHeight(SlidePosition slidePosition) {
        SetSlideHeight(slidePosition, false);
    }

    public void SetSlideHeight(SlidePosition slidePosition, boolean WaitForCompletion) {
        switch (slidePosition) {
            case UpperBasket:
                desiredSlideHeight = slideHeightUpperBasket;
                break;
            case Bottom:
                desiredSlideHeight = slideHeightBottom;
                break;
            case Top:
                desiredSlideHeight = slideHeightTop;
                break;
        }
        if (WaitForCompletion) {
            ElapsedTime timer = new ElapsedTime();
            while (Math.abs(desiredSlideHeight - CurrentSlideHeight()) >= 0.25 && timer.seconds() < 3) {
                PerformSlideMovements();
            }
        }
    }

    private void sleep(int duration) {
        if (!opmode.isStopRequested() || opmode.opModeIsActive()) {
            try {
                Thread.sleep(duration);
            } catch (Exception e) {
            }
        }
    }
}
