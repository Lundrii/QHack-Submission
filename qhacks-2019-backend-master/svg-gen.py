import os
import gerber
import sys
import json
from gerber.render import GerberCairoContext
from gerber.utils import parse_gerber_value
from gerber.rs274x import GerberParser


copper = gerber.read('input.GTL') # argument string is the filename

# Rendering context
ctx = GerberCairoContext()

# Create PNG
copper.render(ctx)
# js = GerberParser.parse('input.GTL')
# print(js)
# # with open('data.json', 'w') as outfile:
# #     json.dump(js, outfile)

# # with open('data.json') as f:
# #     data = json.load(f)
# # print(data[0])

ctx.dump(os.path.join(os.path.dirname(__file__), 'output.png'))
#print("Saved")
exit()


