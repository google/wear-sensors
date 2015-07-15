#!/bin/bash

# ---------------------------------------------------------------------
# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ---------------------------------------------------------------------

cd `dirname $0`

for each in `find . -name "*.java" -o -name "*.c" -o -name "*.h"`; do
  # Need to determine if any license text exists before we try to change it
  SEPARATOR=" ---------------------------------------------------------------------"
  EXISTS=`grep -- "$SEPARATOR" $each`
  if [ "$EXISTS" = "" ]; then
    # No license exists, just add it to the top, along with an extra blank line
    (cat COPYRIGHT; echo; cat $each) > $each.temp
  else
    # License text exists, use sed to remove it
    (cat COPYRIGHT; cat $each | sed "1,/$SEPARATOR/d") > $each.temp
  fi
  mv $each.temp $each
done
