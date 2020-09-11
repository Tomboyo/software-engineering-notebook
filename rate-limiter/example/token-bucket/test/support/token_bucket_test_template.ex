defmodule TokenBucket.TestTemplate do
    import TokenBucket

    defmacro __using__(impl: impl) do
        quote location: :keep do
            use ExUnit.Case

            defp new_bucket(tokens, refresh \\ :never) do
                {:ok, bucket} = TokenBucket.new(unquote(impl), tokens, refresh)
                on_exit(fn -> TokenBucket.close(unquote(impl), bucket) end)
                bucket
            end

            describe "take/2" do
                test "grants requests for available tokens" do
                    bucket = new_bucket(5)
                    assert :granted == take(bucket, 1)
                end
                
                test "denies requests in excess of available tokens" do
                    bucket = new_bucket(5)
                    assert :denied == take(bucket, 6)
                end
                
                test "depletes available tokens when granting requests" do
                    bucket = new_bucket(5)
                    :granted = take(bucket, 5)
                    assert :denied == take(bucket, 1)
                end
                
                test "does not deplete tokens when denying requests" do
                    bucket = new_bucket(5)
                    :denied = take(bucket, 10)
                    assert :granted == take(bucket, 5)
                end
                
                test "can only request a positive number of tokens" do
                    bucket = new_bucket(5)
                    
                    assert_raise ArgumentError, fn -> take(bucket, -1) end
                    assert_raise ArgumentError, fn -> take(bucket, 0) end
                end
            end
            
            defp foo, do: :foo
            
            describe "guard/3" do
                test "invokes callbacks when there are available tokens" do
                    bucket = new_bucket(5)
                    assert {:granted, :foo} == guard(bucket, 1, &foo/0)
                end
                
                test "does not invoke callbacks when there are insufficient tokens" do
                    bucket = new_bucket(5)
                    assert :denied == guard(bucket, 6, &foo/0)
                end
                
                test "depletes available tokens when invoking callbacks" do
                    bucket = new_bucket(5)
                    
                    {:granted, :foo} = guard(bucket, 5, &foo/0)
                    assert :denied == guard(bucket, 1, &foo/0)
                end
                
                test "does not deplete tokens if callbacks are not invoked" do
                    bucket = new_bucket(5)
                    :denied = guard(bucket, 10, &foo/0)
                    assert {:granted, :foo} == guard(bucket, 5, &foo/0)
                end
                
                test "depletes tokens before invoking callbacks" do
                    bucket = new_bucket(5)
                    assert {:granted, :denied} = guard(bucket, 1, fn -> 
                        :denied = take(bucket, 5)
                    end)
                end
                
                test "can only request a positive number of tokens" do
                    bucket = new_bucket(5)
                    
                    assert_raise ArgumentError, fn -> guard(bucket, -1, &foo/0) end
                    assert_raise ArgumentError, fn -> guard(bucket, 0, &foo/0) end
                end
            end
            
            test "guard/3 and take/2 deplete the same tokens" do
                bucket = new_bucket(2)
                :granted = take(bucket, 1)
                {:granted, :foo} = guard(bucket, 1, &foo/0)
                
                assert {:denied, :denied} = {take(bucket, 1), guard(bucket, 1, &foo/0)}
            end
            
            describe "give/2" do
                test "adds tokens to a bucket" do
                    bucket = new_bucket(5)
                    take(bucket, 5)
                    give(bucket, 1)
                    assert :granted == take(bucket, 1)
                end
                
                test "adds tokens up to a bucket's initial capacity" do
                    bucket = new_bucket(5)
                    take(bucket, 1)
                    give(bucket, 2) # 5 - 1 + 2 == 6, more than initial capacity
                    
                    # Tokens in excess of initial capacity (5) are discarded
                    assert :denied == take(bucket, 6)
                    assert :granted == take(bucket, 5)
                end
                
                test "can only add a positive number of tokens" do
                    bucket = new_bucket(5)
                    
                    assert_raise ArgumentError, fn -> give(bucket, -1) end
                    assert_raise ArgumentError, fn -> give(bucket, 0) end
                end
            end
            
            describe "new/2" do
                test "may automatically schedule addition of tokens on a timer" do
                    # refresh all tokens every millisecond
                    bucket = new_bucket(5, [ms: 1, tokens: 5])
                    
                    :granted = take(bucket, 5)
                    :timer.sleep(5)
                    
                    assert :granted == take(bucket, 1)
                end
            end
        end
    end
end
