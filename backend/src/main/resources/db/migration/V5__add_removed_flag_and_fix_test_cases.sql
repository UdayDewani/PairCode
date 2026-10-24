-- Phase 1: add 'removed' flag to room_participants to distinguish explicit removal from natural leave
ALTER TABLE room_participants ADD COLUMN IF NOT EXISTS removed BOOLEAN NOT NULL DEFAULT false;

-- Phase 2: fix V3 test cases that have literal \n instead of actual newline characters
-- Two Sum test case 1: input should be two lines
UPDATE test_cases SET
  input = E'2 7 11 15\n9\n',
  expected_output = E'0 1\n'
WHERE id = '10000000-0000-0000-0000-000000000001';

UPDATE test_cases SET
  input = E'3 2 4\n6\n',
  expected_output = E'1 2\n'
WHERE id = '10000000-0000-0000-0000-000000000002';
