package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.Hardware.RobotController;
import org.firstinspires.ftc.teamcode.Hardware.Claw.ArmPosition;
import org.firstinspires.ftc.teamcode.Hardware.Claw.SlidePosition;



@Autonomous
public class Auto extends LinearOpMode {
    String alliance = "Blue";
    boolean startDelay = false;
    boolean handClosed = true;
    String startDelayMessage = "Push Y to Delay Start";
    Gamepad currentGamepad1 = new Gamepad();
    Gamepad previousGamepad1 = new Gamepad();
    Gamepad currentGamepad2 = new Gamepad();
    Gamepad previousGamepad2 = new Gamepad();
    double autonomousMaxSpeed = 0.5;
    double liftHeight = 36.5;
    
    RobotController robot = new RobotController();

    @Override
    public void runOpMode() {
        ElapsedTime timer = new ElapsedTime();
        robot.init(this, hardwareMap);
        while (!opModeIsActive() && !isStopRequested()) {
            if (currentGamepad1.a && !previousGamepad1.a) {
                if (alliance == "Blue") {
                    alliance = "Red";
                } else {
                    alliance = "Blue";
                }
            }
            if (currentGamepad2.a && !previousGamepad2.a) {
                if (handClosed) {
                    robot.claw.OpenHand();
                } else {
                    robot.claw.CloseHand();
                }
                handClosed = !handClosed;
            }
            if (gamepad2.b) {
                robot.claw.PerformClawMovements(-gamepad2.left_stick_y);
            } else {
                robot.claw.PerformClawMovements(-gamepad2.left_stick_y * 0.5);
            }
            if (gamepad2.left_bumper) {
                robot.claw.SetArmPosition(ArmPosition.Starting);
            } else if (gamepad2.right_bumper) {
                if (currentGamepad2.back) {
                    robot.claw.SetArmPosition(ArmPosition.Down);
                } else {
                    robot.claw.SetArmPosition(ArmPosition.Basket);
                }
            } else if (currentGamepad2.left_trigger != 0) {
                robot.claw.AdjustArmUp();
            } else if (currentGamepad2.right_trigger != 0) {
                robot.claw.AdjustArmDown();
            }
            
            if (currentGamepad1.y && !previousGamepad1.y) {
                if (currentGamepad1.back) {
                    //robot.DriveToTenInches();
                }
                startDelay = !startDelay;
                if (startDelay) {
                    startDelayMessage = "Push Y to not delay start";
                } else {
                    startDelayMessage = "Push Y to Delay Start";
                }
            }
            telemetry.addData("Alliance " + alliance, "Press A on GamePad 1 to switch alliance");
            if (handClosed) {
                telemetry.addData("Alliance " + alliance, "Press A on GamePad 2 to Open Hand");
            } else {
                telemetry.addData("Alliance " + alliance, "Press A on GamePad 2 to Close Hand");
            }
            telemetry.addData("Start Delay " + startDelay, startDelayMessage);
            //telemetry.addData("Distance From Front", robot.sensors.distanceFromObject());
            //double reference = 10.0;
            //double error = reference - robot.sensors.distanceFromObject();
            //telemetry.addData("Bad", Math.abs(error) >= 1.0);
            //telemetry.addData("Timer", timer.seconds());
            telemetry.update();
            previousGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            previousGamepad2.copy(currentGamepad2);
            currentGamepad2.copy(gamepad2);
        }
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        if (opModeIsActive()) {
            robot.DriveStraight(20, autonomousMaxSpeed);
            robot.StopBot();
            robot.driveTrain.TurnToHeading(135);
            robot.DriveStraight(17, autonomousMaxSpeed);
            telemetry.addData("Bot Current Heading", robot.CurrentHeading());
            telemetry.addData("Bot Desired Heading", robot.driveTrain.DesiredHeading());
            telemetry.addData("Distance Travelled", robot.driveTrain.DistanceTravelled());
            telemetry.update();
            if (startDelay) {
                sleep(5000);
            } else {
                sleep(500);
            }
            robot.claw.SetSlideHeight(SlidePosition.UpperBasket, true);
            robot.claw.SetArmPosition(ArmPosition.Basket, true);
            sleep(100);
            robot.claw.OpenHand();
            sleep(500);
            robot.claw.SetArmPosition(ArmPosition.Verticle, true);
            robot.claw.CloseHand();
            robot.claw.SetSlideHeight(SlidePosition.Bottom, true);
            robot.driveTrain.TurnToHeading(-90);
            robot.DriveStraight(110, autonomousMaxSpeed);
            while (opModeIsActive()) {
                telemetry.addData("Status", "Running");
                telemetry.addData("Position", "Should be parked");
                telemetry.update();
            }
        }
    }
}
