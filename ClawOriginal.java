package org.firstinspires.ftc.teamcode.Hardware;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Claw {
    private LinearOpMode opmode;
    private Servo clawHand;
    private Servo clawArm;
    private DcMotor rightSlide;

    private int eventsPerMotorRotation = 28; 
    private double wheelMotorRatio = 19.2;
    private double eventsPerWheelRotation = eventsPerMotorRotation * wheelMotorRatio;
    private double inchesPerWheelRotation = 4.7244;
    private boolean rightSlideStopped = true;

    private boolean handClosed = false;
    
    private double handClosedPosition = 0.30;
    private double handOpenPosition = 0.1;
    
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
        clawHand = hwMap.get(Servo.class, "clawHand");
        clawArm = hwMap.get(Servo.class, "clawArm");
        rightSlide = hwMap.dcMotor.get("rightSlide");
        rightSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightSlide.setDirection(DcMotorSimple.Direction.REVERSE);
        rightSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        SetClawToStartPosition();
    }

    public void PerformClawMovements(double slidePower) {
        if (slidePower == 0) {
            PerformSlideMovements();
        } else {
            SetSlidePower(slidePower, true);
        }
        PerformArmMovements();
    }
    
    private void SetClawToStartPosition() {
        clawArm.setPosition(desiredArmPosition);
        CloseHand();
        sleep(100);
        SetArmPosition(ArmPosition.Starting);
        while (Math.abs(desiredArmPosition - clawArm.getPosition()) >= 0.008) {
            if (clawArm.getPosition() > desiredArmPosition) {
                clawArm.setPosition(clawArm.getPosition() - 0.01);
            } else {
                clawArm.setPosition(clawArm.getPosition() + 0.01);
            }
            sleep(40);
        }
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
            if (!rightSlideStopped) {
                rightSlide.setPower(0);
                sleep(10);
                rightSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                rightSlide.setTargetPosition(rightSlide.getCurrentPosition());
                rightSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                rightSlide.setPower(0.1);
                rightSlideStopped = true;
            }
        } else {
            rightSlideStopped = false;
            rightSlide.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightSlide.setPower(slidePower);
        }
        if (Manual) {
            desiredSlideHeight = CurrentSlideHeight();
        }
    }

    public double CurrentSlidePower() {
        return rightSlide.getPower();
    }

    public double CurrentSlideHeight() {
        return rightSlide.getCurrentPosition() / eventsPerWheelRotation * inchesPerWheelRotation;
    }

    private double DesiredSlidePower(double remainingRotations, double maxPower) {
        if (remainingRotations <= 0) {
            return 0;
        }
        double desiredPower = Math.min(maxPower, remainingRotations);
        desiredPower = Math.max(desiredPower, 0.2);
        return Math.round(desiredPower * 20.0) / 20.0;
    }

    private void PerformSlideMovements() {
        double distanceToMove = desiredSlideHeight - CurrentSlideHeight();
        double motorPowerIncrement = 0.05;
        if (Math.abs(distanceToMove) > 0.1) {
            double neededWheelRotations = distanceToMove / inchesPerWheelRotation;
            double remainingRotations = Math.abs(neededWheelRotations);
            double desiredPower =  Math.signum(neededWheelRotations) * DesiredSlidePower(remainingRotations, maxSlidePower);
            double currentPower = rightSlide.getPower();
            double powerDifference = desiredPower - currentPower;
            if (powerDifference > motorPowerIncrement) {
                desiredPower = Math.min(desiredPower, currentPower + motorPowerIncrement);
            } else if (powerDifference < -motorPowerIncrement) {
                desiredPower = Math.max(desiredPower, currentPower - motorPowerIncrement);
            }
            SetSlidePower(desiredPower, false);
        }
        else {
            SetSlidePower(0, false);
        }
    }

    private void PerformArmMovements() {
        double positionsToMove = desiredArmPosition - clawArm.getPosition();
        double positionsToAdjust = 0;
        if (Math.abs(positionsToMove) >= 0.1) {
            positionsToAdjust = 0.05;
        } else if (Math.abs(positionsToMove) >= 0.05) {
            positionsToAdjust = 0.025;
        } else if (Math.abs(positionsToMove) >= 0.008) {
            positionsToAdjust = 0.01;
        }
        if (positionsToAdjust != 0) {
            clawArm.setPosition(clawArm.getPosition() + Math.signum(positionsToMove)*positionsToAdjust);
            sleep(20);
        }
    }

    public void OpenHand() {
        if (handClosed) {
            clawHand.setPosition(handOpenPosition);
            handClosed = false;
        }
    }

    public void CloseHand() {
        if (!handClosed) {
            clawHand.setPosition(handClosedPosition);
            handClosed = true;
        }
    }

    public void SetArmPosition(ArmPosition armPosition, boolean WaitForCompletion) {
        switch (armPosition) {
            case Basket:
                desiredArmPosition = armStartingBasket;
                break;
            case Starting:
                desiredArmPosition = armStartingStarting;
                break;
            case Verticle:
                desiredArmPosition = armPositionVerticle;
                break;
            case Down:
                desiredArmPosition = armPositionDown;
                break;
        }
        if (WaitForCompletion) {
            while ((Math.abs(desiredArmPosition - clawArm.getPosition()) >= 0.008))
            {
                PerformArmMovements();
            }
        }
    }

    public void SetArmPosition(ArmPosition armPosition) {
        SetArmPosition(armPosition, false);
    }

    public void AdjustArmUp() {
        desiredArmPosition += 0.01;
        sleep(25);
    }

    public void AdjustArmDown() {
        desiredArmPosition -= 0.01;
        sleep(25);
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
            while (Math.abs(desiredSlideHeight - CurrentSlideHeight()) >= 0.25 && timer.seconds() < 3)
            {
                PerformSlideMovements();
            }
        }
    }
    public void SetSlideHeight(SlidePosition slidePosition) {
        SetSlideHeight(slidePosition, false);
    }

    private void sleep (int duration) {
        if (!opmode.isStopRequested() || opmode.opModeIsActive()) {
            try {Thread.sleep(duration);}
            catch (Exception e) {}
        }
    }
}
