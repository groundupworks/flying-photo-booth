#! /usr/bin/env python
import sys
import os

try:
    sys.path.append(os.environ['YKSP_HOME'])
except:
    pass

from yksptestcase import YkspTestCase

NUM_CAPTURE_LOOPED = 3

class CaptureModeSelfServeTestCase(YkspTestCase):

    def testCaptureLooped(self):
        '''
        Tests the Self-serve Mode capture sequence NUM_CAPTURE_LOOPED times.
        '''
        self.launchApp()
        self.refreshScreen()

        # Set up
        self.vc.findViewWithTextOrRaise('Next').touch()
        self.refreshScreen()
        self.vc.findViewByIdOrRaise('com.groundupworks.partyphotobooth:id/setup_photo_booth_mode').touch()
        self.refreshScreen()
        self.vc.findViewWithTextOrRaise('Self-serve').touch()
        self.saveScreen('self-serve-selection')
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
            self.refreshScreen(sleep=6)
            text = self.vc.findViewWithTextOrRaise('2 of 3')
            self.vc.findViewByIdOrRaise('com.groundupworks.partyphotobooth:id/capture_button').touch()
            self.refreshScreen(sleep=6)
            text = self.vc.findViewWithTextOrRaise('3 of 3')
            self.vc.findViewByIdOrRaise('com.groundupworks.partyphotobooth:id/capture_button').touch()
            self.saveScreen('self-serve-ps%s' % i, sleep=6)
            self.vc.findViewWithTextOrRaise('Thank you for using\nParty PhotoBooth')
            self.vc.findViewWithTextOrRaise('Submit').touch()
            self.refreshScreen()


if __name__ == '__main__':
    YkspTestCase.main(sys.argv)