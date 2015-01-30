  Uses: Remoting
  Uses: Processes

  Feature: I can travel by snowmobile

    #! Processes start pub, sub
    Feature-Start:

    ### Snow Mobile

    #! Remoting use pub, sub
    Scenario: I can travel by snowmobile when it is freezing
      When I set the temperature to 0
      Then I can travel by snowmobile

    #! Remoting use pub, sub
    Scenario: I can't travel by snowmobile when it is subtropical
      When I set the temperature to 30
      Then I can't travel by snowmobile

    #! Remoting use pub, sub
    Scenario: I can travel by snowmobile when it is polar
      When I set the temperature to -20
      Then I can travel by snowmobile



    ### Balloon

    #! Remoting use pub, sub
    Scenario: I can fly by balloon when wind is less than five and precipitation not fish
      Given I set temp, wind and precipitation to 0, 4, None
      Then I can travel by balloon

      #! Remoting use pub, sub
    Scenario: I cannot fly when precipitation is fish
      Given I set temp, wind and precipitation to 0, 4, Fish
      Then I can't travel by balloon

      #! Remoting use pub, sub
    Scenario: I cannot fly when wind is 5 or higher
      Given I set temp, wind and precipitation to 0, 5, None
      Then I can't travel by balloon

      #! Remoting use pub, sub
    Scenario: I can fly in the arctic
      Given I set temp, wind and precipitation to -20, 4, Snow
      Then I can travel by balloon

      #! Remoting use pub, sub
    Scenario: I can fly in the tropics
      Given I set temp, wind and precipitation to 40, 0, Rain
      Then I can travel by balloon



    ### Train

    #! Remoting use pub, sub
    Scenario: I can commute by Thameslink when temperature ideal with no wind and raining fish
      Given I set temp, wind and precipitation to 18, 0, Fish
      Then I can travel by train

      #! Remoting use pub, sub
    Scenario: Thameslink trains do not run in all other circumstances
      Given I set temp, wind and precipitation to 19, 0, Fish
      Then I can't travel by train
      Or I set temp, wind and precipitation to 18, 1, Fish
      Then I can't travel by train
      Or I set temp, wind and precipitation to 18, 0, None
      Then I can't travel by train



    ### Pressure

      #! Remoting use pub, sub
    Scenario: Sending pressure will cause the pressure difference to be calculated
      Given I set the pressure to 200, 300
      Then the pressure difference is 100

      #! Remoting use pub, sub
    Scenario: Pressure deltas are processed atomically
      Given I set the pressure to 600, 700
      And the pressure difference is 100
      When I set the pressure to 800, 1000
      Then the pressure difference is 200
      And the last pressure difference is 100
