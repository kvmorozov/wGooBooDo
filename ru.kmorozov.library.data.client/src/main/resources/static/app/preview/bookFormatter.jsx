import React from "react";
import {Icon} from "semantic-ui-react";

class BookFormatter extends React.Component {
    render() {
        let iconName;
        switch (this.props.format) {
            case 'PDF':
                iconName = 'file pdf outline';
                break;
            case 'DJVU':
                iconName = 'file text';
                break;
        }

        return (
            <Icon disabled name={iconName} />
        );
    }
}

export default BookFormatter;