## Check Existing Access Token
To avoid charging a cardholder more than once we recommend that you store the created access tokens against a session or order. Then when the payment page is refreshed check the status of the previous access token and revoke it if it's not used before requesting another. If the original access token has already been used and the payment was successful, update the order accordingly otherwise request a new access token.

We have seen issues when new access tokens are issued on page load then there is a connectivity loss during the transaction, causing the cardholder to be charged but not shown the payment complete page. When the cardholder refreshes the page a new access token is created and they’re charged again.

## Wait Pre-execute
To have the most control over a transaction we recommend using the Wait Pre-execute payment flow. The transaction is only executed when the resume endpoint is called. This reduces the chance that there are connectivity issues or errors in JavaScript causing a cardholder to be charged without their order being updated.

## Webhooks
Due to the nature of distributed systems there is always a chance of network calls failing. We recommend updating your order via webhook. As we use an exponential back off we will attempt to call your endpoint for up to 2 hours allowing for tolerances of outages or network connectivity issues.

Storing Expiry Date for Repeat Payments
If you are making repeat payments we recommend you also store the expiry date in addition to the cross reference. Then you can choose to recapture card details if the original card has expired.

## Unique Order ID
Each order should pass a unique and identifiable order ID in the OrderId field, which can be used to reconcile transactions between the website and Connect-E during troubleshooting. The OrderId should be the same for additional access tokens created when reattempting a payment after a decline.

## Suggested Testing
Once the integration is complete we recommend testing the following scenarios.

3DS payment flow. e.g. using card 4456530000001096
Standard payment flow e.g. using card 5200000000000056
Test declined payment. Does the integration request a new access token and redisplay/update the payment form? e.g. using test card 4456530000001013
If using PREAUTH transactions. Can the integration successfully COLLECT the PREAUTH
If the integration uses the cross reference to execute recurring payments does this work for both 3DS cards and standard cards?
Does this allow the user to see which card is being used?
Does this hide the option once the card has expired?