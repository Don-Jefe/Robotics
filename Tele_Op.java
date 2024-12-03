package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.Hardware.RobotController;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Hardware.Claw.ArmPosition;
import org.firstinspires.ftc.teamcode.Hardware.Claw.SlidePosition;

@TeleOp

public class Tele_Op extends LinearOpMode {
    RobotController robot = new RobotController();
    boolean fieldCentricDriving = false;
    Gamepad currentGamepad1 = new Gamepad();
    Gamepad previousGamepad1 = new Gamepad();
    Gamepad currentGamepad2 = new Gamepad();
    Gamepad previousGamepad2 = new Gamepad();
    double speedMultiplier = 1;
    boolean speedControl = true;

    @Override
    public void runOpMode() {
        robot.init(this, hardwareMap);
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            currentGamepad1.copy(gamepad1);
            currentGamepad2.copy(gamepad2);
            if (gamepad1.b) {
                speedMultiplier = 1;
            } else {
                speedMultiplier = 0.6;
            }
            if (gamepad1.back) {
                if (gamepad1.a) {robot.ResetForwardPosition();}
                if (currentGamepad1.y && !previousGamepad1.y) {fieldCentricDriving = !fieldCentricDriving;}
            } else {
                if (gamepad1.dpad_up) {robot.SetHeading(0);}
                else if (gamepad1.dpad_down) {robot.SetHeading(180);}
                else if (gamepad1.dpad_left) {robot.SetHeading(90);}
                else if (gamepad1.dpad_right) {robot.SetHeading(-90);}
            }
            if (gamepad2.a) {
                robot.claw.OpenHand();
            } else {
                robot.claw.CloseHand();
            }
            if (currentGamepad2.dpad_up && !previousGamepad2.dpad_up) {
                robot.claw.SetSlideHeight(SlidePosition.UpperBasket);
            } else if  (currentGamepad2.dpad_down && !previousGamepad2.dpad_down) {
                robot.claw.SetSlideHeight(SlidePosition.Bottom);
            }           
            if (gamepad2.b) {
                robot.claw.PerformClawMovements(-gamepad2.left_stick_y);
            } else {
                robot.claw.PerformClawMovements(-gamepad2.left_stick_y * 0.5);
            }
            if (gamepad2.left_bumper) {
                robot.claw.SetArmPosition(ArmPosition.Verticle);
            } else if (gamepad2.right_bumper) {
                if (robot.claw.CurrentSlideHeight() <= 10) {
                    robot.claw.SetArmPosition(ArmPosition.Down);
                } else {
                    robot.claw.SetArmPosition(ArmPosition.Basket);
                }
            } else if (currentGamepad2.left_trigger != 0) {
                robot.claw.AdjustArmUp();
            } else if (currentGamepad2.right_trigger != 0) {
                robot.claw.AdjustArmDown();
            }

            double sideways = 0;
            double forward = 0;
            double rotation = 0;
            if (speedControl == true) {
                sideways = gamepad1.left_stick_x * Math.abs(gamepad1.left_stick_x) * speedMultiplier;
                forward = -gamepad1.left_stick_y * Math.abs(gamepad1.left_stick_y) * speedMultiplier;
                rotation = gamepad1.right_stick_x * Math.abs(gamepad1.right_stick_x) * speedMultiplier;
            } else {
                sideways = gamepad1.left_stick_x * speedMultiplier;
                forward = -gamepad1.left_stick_y * speedMultiplier;
                rotation = gamepad1.right_stick_x * speedMultiplier;
            } 
            robot.Drive(sideways, forward, rotation * 0.4, fieldCentricDriving);
            telemetry.addData("Speed Multiplier", speedMultiplier);
            telemetry.addData("Field Centric Driving (back+y)", fieldCentricDriving);
            telemetry.addData("Rotation Speed", rotation);
            telemetry.addData("sideways", sideways);
            telemetry.addData("forward", forward);
            telemetry.addData("Slide Height", robot.claw.CurrentSlideHeight());
            telemetry.addData("Slide Power", robot.claw.CurrentSlidePower());
            telemetry.update();
            previousGamepad1.copy(currentGamepad1);
            previousGamepad2.copy(currentGamepad2);
        }
    }
}
