#! /usr/bin/env python
import sys
import os

try:
    sys.path.append(os.environ['YKSP_HOME'])
except:
    pass

from yksptestcase import YkspTestCase

NUM_CAPTURE_LOOPED = 3

class CaptureModeAutomaticTestCase(YkspTestCase):

    def testCaptureLooped(self):
        '''
        Tests the Automatic Mode capture sequence NUM_CAPTURE_LOOPED times.
        '''
        self.launchApp()
        self.refreshScreen()

        # Set up
        self.vc.findViewWithTextOrRaise('Next').touch()
        self.refreshScreen()
        self.vc.findViewByIdOrRaise('com.groundupworks.partyphotobooth:id/setup_photo_booth_mode').touch()
        self.refreshScreen()
        self.vc.findViewWithTextOrRaise('Automatic').touch()
        self.saveScreen('automatic-selection')
        self.vc.findViewWithTextOrRaise('Next').touch()
        self.refreshScreen()
        self.vc.findViewWithTextOrRaise('Next').touch()
        self.refreshScreen()
        self.vc.findViewWithTextOrRaise('OK').touch()
        self.refreshScreen()

        # Run capture sequence NUM_CAPTURE_LOOPED times
        for i in range(0, NUM_CAPTURE_LOOPED):
            text = self.vc.findViewWithTextOrRaise('1 of 3')
            self.vc.findViewByIdOrRaise('com.groundupworks.partyphotobooth:id/capture_button').touch()
            self.saveScreen('automatic-ps%s' % i, sleep=20)
            self.vc.findViewWithTextOrRaise('Thank you for using\nParty PhotoBooth')
            self.assertIsNone(self.vc.findViewWithText('Submit'))
            self.refreshScreen(sleep=20)
            self.refreshScreen()


if __name__ == '__main__':
    YkspTestCase.main(sys.argv)